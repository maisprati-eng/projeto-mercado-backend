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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.prati.projetomercado.exceptions.EmailAlreadyExistsException;
import java.util.Comparator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
        if (createUserRequest.password().length() < 8) {
            throw new BadCredentialsException(List.of(new FieldError("password", "Min length: 8 characters")));
        }

        Optional<AuthUser> existingUserOpt = userRepository.findByEmail(createUserRequest.email());

        if (existingUserOpt.isPresent()) {
            AuthUser existingUser = existingUserOpt.get();

            if (existingUser.isEnabled()) {
                throw new EmailAlreadyExistsException("Este e-mail já está em uso por uma conta ativa.");
            } else {
                updateUnconfirmedUser(existingUser, createUserRequest);
            }
        } else {
            createNewUser(createUserRequest);
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
            log.error(String.valueOf(e.fillInStackTrace()));
            throw new AuthException("Usuário não encontrado");
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        var user = userDetailsImpl.getAuthUser();

        if (!user.isEnabled()) {
            // Se o usuário não estiver ativo, lança uma exceção e impede o login.
            throw new AuthException("Por favor, confirme seu e-mail para ativar sua conta.");
        }
        // Substituindo o bloco "accessTokenEntityOld"

        // 1. (Parte 2 da Issue) LIMPAR TOKENS EXPIRADOS
        // Busca TODOS os tokens do usuário
        List<AccessToken> userTokens = accessTokenRepository.findAllByAuthUser(user);

        // Filtra e prepara para deletar os expirados
        List<AccessToken> expiredTokens = userTokens.stream()
                .filter(token -> token.getExpiredDate().isBefore(Instant.now()))
                .toList();

        if (!expiredTokens.isEmpty()) {
            // Deleta todos os tokens expirados de uma vez
            accessTokenRepository.deleteAll(expiredTokens);
        }

        // 2. (Parte 1 da Issue) LIMITAR A 3 TOKENS ATIVOS
        // Filtra os tokens que ainda estão ativos
        List<AccessToken> activeTokens = userTokens.stream()
                .filter(token -> token.getExpiredDate().isAfter(Instant.now()))
                .toList();

        // Se o usuário já tem 3 ou mais tokens ativos, apaga o mais antigo
        if (activeTokens.size() >= 3) {
            // Encontra o token mais antigo (com a menor data de expiração)
            AccessToken oldestToken = activeTokens.stream()
                    .min(Comparator.comparing(AccessToken::getExpiredDate))
                    .orElse(null);

            if (oldestToken != null) {
                accessTokenRepository.delete(oldestToken);
            }
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

    // ADICIONE ESTES DOIS MÉTODOS NO FINAL DA SUA CLASSE UserServiceImpl

    private void createNewUser(CreateUserRequest request) {
        // Esta é a sua lógica de criação que já existe (com o if/else do modo dev)
        if (emailConfirmationEnabled) {
            String confirmationToken = UUID.randomUUID().toString();
            AuthUser newUser = AuthUser.builder()
                    .email(request.email())
                    .username(request.username())
                    .password(encoder.encode(request.password()))
                    .enabled(false)
                    .confirmationToken(confirmationToken)
                    .confirmationTokenExpiry(LocalDateTime.now().plusHours(24))
                    .build();
            AuthUser savedUser = userRepository.save(newUser);
            emailService.sendConfirmationEmail(savedUser);
        } else {
            // Modo dev: já cria o usuário ativo
            AuthUser newUser = AuthUser.builder()
                    .email(request.email())
                    .username(request.username())
                    .password(encoder.encode(request.password()))
                    .enabled(true)
                    .build();
            userRepository.save(newUser);
        }
    }

    private void updateUnconfirmedUser(AuthUser userToUpdate, CreateUserRequest request) {
        userToUpdate.setUsername(request.username());
        userToUpdate.setPassword(encoder.encode(request.password()));

        if (emailConfirmationEnabled) {
            String newConfirmationToken = UUID.randomUUID().toString();
            userToUpdate.setConfirmationToken(newConfirmationToken);
            userToUpdate.setConfirmationTokenExpiry(LocalDateTime.now().plusHours(24));
        } else {
            userToUpdate.setEnabled(true);
            userToUpdate.setConfirmationToken(null);
            userToUpdate.setConfirmationTokenExpiry(null);
        }

        AuthUser updatedUser = userRepository.save(userToUpdate);

        if (emailConfirmationEnabled) {
            emailService.sendConfirmationEmail(updatedUser);
        }
    }
}
