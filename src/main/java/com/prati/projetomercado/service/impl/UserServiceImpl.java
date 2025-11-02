package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.dto.request.ChangePasswordRequest;
import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.response.AuthResponse;
import com.prati.projetomercado.dto.response.UserResponse;
import com.prati.projetomercado.entity.AccessToken;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.FieldError;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.RefreshTokenRepository;
import com.prati.projetomercado.service.EmailService;
import com.prati.projetomercado.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.prati.projetomercado.dto.request.SearchRequestDTO;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository userRepository;
    private final JwtTokenServiceImpl jwtTokenService;
    private final PasswordEncoder encoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final EmailService emailService;

    @Value("${email.confirmation.enabled}")
    private boolean emailConfirmationEnabled;


    @Override
    public void registerUser(CreateUserRequest createUserRequest) {
        if (!createUserRequest.password().equals(createUserRequest.confirmPassword())) {
            throw new BadCredentialsException(
                    List.of(new FieldError("confirmPassword", "Passwords don't match"), new FieldError("password", "Passwords don't match")));
        }

        if (createUserRequest.password().length() < 8) { // Ajustado para 8, conforme seu controller
            throw new BadCredentialsException(List.of(new FieldError("password", "Min length: 8 characters")));
        }
        // A NOVA LÓGICA CONDICIONAL INTERRUPTOR
        if (emailConfirmationEnabled) {
            // --- CENÁRIO 1: ENVIO DE E-MAIL LIGADO --- email.confirmation.enabled=true
            String confirmationToken = UUID.randomUUID().toString();
            AuthUser newUser = AuthUser.builder()
                    .email(createUserRequest.email())
                    .username(createUserRequest.username())
                    .password(encoder.encode(createUserRequest.password()))
                    .enabled(false) // Começa desativado
                    .confirmationToken(confirmationToken)
                    .confirmationTokenExpiry(LocalDateTime.now().plusHours(24))
                    .build();
            AuthUser savedUser = userRepository.save(newUser);
            emailService.sendConfirmationEmail(savedUser);
        } else {
            // --- CENÁRIO 2: ENVIO DE E-MAIL DESLIGADO (MODO DEV) --- email.confirmation.enabled=false
            AuthUser newUser = AuthUser.builder()
                    .email(createUserRequest.email())
                    .username(createUserRequest.username())
                    .password(encoder.encode(createUserRequest.password()))
                    .enabled(true) // Já começa ativado
                    .build();
            userRepository.save(newUser);
        }
    }

    @Override
    public AuthUser registerOAuth2User(String username, String email) {
        Optional<AuthUser> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        AuthUser newUser = AuthUser.builder()
                .email(email)
                .username(username)
                .enabled(true)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public AuthResponse login(LoginUserRequest loginUserRequest) throws Exception {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            throw new AuthException("Auth manager error");
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        var user = userDetailsImpl.getAuthUser();

        if (!user.isEnabled()) {
            // Se o usuário não estiver ativo, lança uma exceção e impede o login.
            throw new AuthException("Por favor, confirme seu e-mail para ativar sua conta.");
        }

        var accessTokenEntityOld = accessTokenRepository.findByAuthUser(user);

        if (accessTokenEntityOld != null) {
            accessTokenRepository.delete(accessTokenEntityOld);
        }

        var refreshToken = jwtTokenService.generateNewRefreshToken(userDetailsImpl.getAuthUser());

        refreshTokenRepository.save(refreshToken);
        var accessTokenExpirationDate = jwtTokenService.expirationAccessTokenDate();
        var accessToken = jwtTokenService.generateToken(userDetailsImpl.getAuthUser(), accessTokenExpirationDate);
        var accessTokenEntity = AccessToken.builder().authUser(userDetailsImpl.getAuthUser()).token(accessToken).expiredDate(accessTokenExpirationDate).build();
        accessTokenRepository.save(accessTokenEntity);

        var id = userDetailsImpl.getAuthUser().getId();
        var username = userDetailsImpl.getAuthUser().getUsername();
        var email = userDetailsImpl.getAuthUser().getEmail();
        var creationDate = userDetailsImpl.getAuthUser().getCreationDate();

        return new AuthResponse(accessToken, refreshToken.getId(), new UserResponse(id, username, email, creationDate));
    }

    private Optional<AuthUser> getAuthUser(String accessToken) {
        var email = jwtTokenService.getSubjectFromExpiredToken(accessToken);
        return userRepository.findByEmail(email);


    }

    @Override
    public JwtToken useRefreshToken(String accessToken, UUID refreshTokenId) {


        var accessTokenEntity = accessTokenRepository.findByToken(accessToken)
                .orElseThrow(() -> new AuthException("Token foi invalidado (logout realizado)"));

        final var refreshToken = refreshTokenRepository
                .findByIdAndExpiresAtAfter(refreshTokenId, Instant.now())
                .orElseThrow(() -> new AuthException("No refreshToken found / refreshToken expired"));

        if (refreshToken.isAlreadyUsed()) {
            throw new AuthException("Token has already been used");
        }

        if (!refreshToken.getAuthUser().equals(accessTokenEntity.getAuthUser())) {
            throw new AuthException("Incompatibilidade de tokens.");
        }

        refreshToken.setAlreadyUsed(true);
        refreshTokenRepository.save(refreshToken);
        accessTokenRepository.delete(accessTokenEntity);

        var authuser = refreshToken.getAuthUser();

        var newRefreshToken = jwtTokenService.generateNewRefreshToken(authuser);
        refreshTokenRepository.save(newRefreshToken);

        var newAccessExp = jwtTokenService.expirationAccessTokenDate();
        var newAccessToken = jwtTokenService.generateToken(authuser, newAccessExp);

        var newAccessEntity = AccessToken.builder()
                .authUser(authuser)
                .token(newAccessToken)
                .expiredDate(newAccessExp)
                .build();
        accessTokenRepository.save(newAccessEntity);

        return new JwtToken(newAccessToken, newRefreshToken.getId());
    }

    @Override
    public UserResponse getUserInfo() {
        // 1. Pega o email do usuário a partir do token de segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Busca o usuário completo no banco de dados usando o email
        AuthUser authUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // 3. Converte a entidade AuthUser para o nosso DTO de resposta seguro
        return new UserResponse(
                authUser.getId(),
                authUser.getUsername(),
                authUser.getEmail(),
                authUser.getCreationDate()
        );
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        // 1. Pega o email do usuário a partir do token de segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthUser currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        // 2. Verifica se a "senha atual" fornecida bate com a senha salva no banco.
        // O passwordEncoder.matches() compara a senha em texto plano com a senha criptografada.
        if (!encoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("currentPassword", "A senha atual está incorreta.")));
        }

        // 3. Verifica se a "nova senha" e a "confirmação" são iguais.
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("confirmNewPassword", "A nova senha e a confirmação não conferem.")));
        }

        // 4. (Opcional, mas recomendado) Adicionar validações para a nova senha.
        if (request.newPassword().length() < 8) {
            throw new BadCredentialsException(List.of(new FieldError("newPassword", "A nova senha deve ter no mínimo 8 caracteres.")));
        }

        // 5. Se todas as verificações passaram, criptografa e atualiza a senha.
        currentUser.setPassword(encoder.encode(request.newPassword()));

        // 6. Salva o usuário com a nova senha no banco de dados.
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void confirmUser(String token) {
        // 1. Busca o usuário pelo token de confirmação
        AuthUser user = userRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new AuthException("Token de confirmação inválido ou não encontrado."));

        // 2. Verifica se o token já expirou
        if (user.getConfirmationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException("Token de confirmação expirado.");
        }

        // 3. Ativa o usuário
        user.setEnabled(true);

        // 4. Limpa o token para que não possa ser usado novamente (segurança)
        user.setConfirmationToken(null);
        user.setConfirmationTokenExpiry(null);

        // 5. Salva as alterações no banco de dados
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> searchUsers(SearchRequestDTO searchRequest) {

        // 1. Criamos uma "Specification" (uma consulta dinâmica)
        Specification<AuthUser> spec = (root, query, criteriaBuilder) -> {

            // 2. Criamos uma lista de "predicados" (as condições WHERE)
            List<Predicate> predicates = new ArrayList<>();

            // 3. Adicionamos condições à lista APENAS SE o campo não for nulo

            // Exemplo para "fullName" (usando LIKE)
            if (searchRequest.fullName() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")), // Pega o campo 'username'
                        "%" + searchRequest.fullName().toLowerCase() + "%" // Compara com o valor
                ));
            }

            // Exemplo para "email" (usando equal)
            if (searchRequest.email() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("email"),
                        searchRequest.email()
                ));
            }

            // (Aqui você adicionaria a lógica para os outros campos...
            // A lógica para 'ageRange' seria mais complexa,
            // mas vamos focar nos campos de texto primeiro)

            // 4. Combinamos todos os filtros com "AND"
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 5. Executamos a busca com a consulta dinâmica
        List<AuthUser> usersFound = userRepository.findAll(spec);

        // 6. Convertemos a lista de Entidades para DTOs de Resposta
        return usersFound.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreationDate()
                ))
                .collect(Collectors.toList());
    }
}
