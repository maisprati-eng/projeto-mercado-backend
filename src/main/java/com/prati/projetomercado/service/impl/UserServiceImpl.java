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
import com.prati.projetomercado.exceptions.BadRequestException;
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
        String rawPassword = getPassword(createUserRequest);

        // Fluxo normal de criação
        if (emailConfirmationEnabled) {
            // Envio de confirmação por e-mail
            String confirmationToken = UUID.randomUUID().toString();
            AuthUser newUser = AuthUser.builder()
                    .email(createUserRequest.email())
                    .username(createUserRequest.username())
                    .password(encoder.encode(rawPassword))
                    .enabled(false)
                    .confirmationToken(confirmationToken)
                    .confirmationTokenExpiry(LocalDateTime.now().plusHours(24))
                    .build();

            AuthUser savedUser = userRepository.save(newUser);
            emailService.sendConfirmationEmail(savedUser);

        } else {
            // Modo dev — sem confirmação
            AuthUser newUser = AuthUser.builder()
                    .email(createUserRequest.email())
                    .username(createUserRequest.username())
                    .password(encoder.encode(rawPassword))
                    .enabled(true)
                    .build();

            userRepository.save(newUser);
        }
    }

    private static String getPassword(CreateUserRequest createUserRequest) {
        String rawPassword = createUserRequest.password();

        // Validação de senha curta
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BadRequestException(
                    "Requisição inválida",
                    List.of(new FieldError("password", "A senha deve ter pelo menos 6 caracteres."))
            );
        }

        // Confirmação de senha
        String confirm = createUserRequest.confirmPassword();
        if (confirm != null && !rawPassword.equals(confirm)) {
            throw new BadCredentialsException(
                    List.of(new FieldError("confirmPassword", "As senhas não conferem."))
            );
        }
        return rawPassword;
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
        var token = new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (Exception e) {
            throw new AuthException("Auth manager error");
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        var user = userDetailsImpl.getAuthUser();

        if (!user.isEnabled()) {
            throw new AuthException("Por favor, confirme seu e-mail para ativar sua conta.");
        }

        var oldAccessToken = accessTokenRepository.findByAuthUser(user);
        if (oldAccessToken != null) {
            accessTokenRepository.delete(oldAccessToken);
        }

        var refreshToken = jwtTokenService.generateNewRefreshToken(user);
        refreshTokenRepository.save(refreshToken);

        var accessTokenExp = jwtTokenService.expirationAccessTokenDate();
        var accessToken = jwtTokenService.generateToken(user, accessTokenExp);
        var accessTokenEntity = AccessToken.builder()
                .authUser(user)
                .token(accessToken)
                .expiredDate(accessTokenExp)
                .build();
        accessTokenRepository.save(accessTokenEntity);

        return new AuthResponse(
                accessToken,
                refreshToken.getId(),
                new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreationDate())
        );
    }

    private Optional<AuthUser> getAuthUser(String accessToken) {
        var email = jwtTokenService.getSubjectFromToken(accessToken);
        return userRepository.findByEmail(email);
    }

    @Override
    public JwtToken useRefreshToken(String accessToken, UUID refreshTokenId) {
        final var refreshToken = refreshTokenRepository
                .findByIdAndExpiresAtAfter(refreshTokenId, Instant.now())
                .orElseThrow(() -> new AuthException("No refreshToken found / refreshToken expired"));

        if (refreshToken.isAlreadyUsed()) {
            throw new AuthException("Token has already been used");
        }

        refreshToken.setAlreadyUsed(true);
        refreshTokenRepository.save(refreshToken);

        var user = getAuthUser(accessToken).orElseThrow(() -> new AuthException("user not found"));

        accessTokenRepository.findByAuthUserAndToken(user, accessToken)
                .ifPresent(accessTokenRepository::delete);

        var newRefreshToken = jwtTokenService.generateNewRefreshToken(user);
        refreshTokenRepository.save(newRefreshToken);

        var newAccessExp = jwtTokenService.expirationAccessTokenDate();
        var newAccessToken = jwtTokenService.generateToken(user, newAccessExp);

        var newAccessEntity = AccessToken.builder()
                .authUser(user)
                .token(newAccessToken)
                .expiredDate(newAccessExp)
                .build();
        accessTokenRepository.save(newAccessEntity);

        return new JwtToken(newAccessToken, newRefreshToken.getId());
    }

    @Override
    public UserResponse getUserInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreationDate());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthUser currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        if (!encoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("currentPassword", "A senha atual está incorreta.")));
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("confirmNewPassword", "A nova senha e a confirmação não conferem.")));
        }

        if (request.newPassword().length() < 8) {
            throw new BadCredentialsException(List.of(new FieldError("newPassword", "A nova senha deve ter no mínimo 8 caracteres.")));
        }

        currentUser.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void confirmUser(String token) {
        AuthUser user = userRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new AuthException("Token de confirmação inválido ou não encontrado."));

        if (user.getConfirmationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException("Token de confirmação expirado.");
        }

        user.setEnabled(true);
        user.setConfirmationToken(null);
        user.setConfirmationTokenExpiry(null);
        userRepository.save(user);
    }
}
