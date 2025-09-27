package com.prati.projetomercado.controller;


import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.request.RefreshTokenRequest;
import com.prati.projetomercado.dto.response.ErrorResponse;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.UserService;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro, login e gerenciamento de tokens")
public class AuthController {

    private final JwtTokenServiceImpl jwtTokenServiceImpl;
    private AuthUserRepository userRepository;
    private UserService userService;
    private JwtTokenServiceImpl tokenService;

    public AuthController(AuthUserRepository userRepository, UserService userService, JwtTokenServiceImpl tokenService, JwtTokenServiceImpl jwtTokenServiceImpl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtTokenServiceImpl = jwtTokenServiceImpl;
    }

    @Operation(summary = "Realiza o cadastro do usuário", description = "O usuario agora pode realizar o login e obter o token.")
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody CreateUserRequest userRequest) {
        String username = userRequest.username();
        String password = userRequest.password();
        String confirmPassword = userRequest.confirmPassword();

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("as senhas não conferem"));
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body(new ErrorResponse("as senhas devem ter no mínimo 8 caracteres"));
        }

        userService.registerUser(userRequest);

        return ResponseEntity.ok("Usuário cadastrado com sucesso!");
    }

    @Operation(summary = "Realiza o login do usuário", description = "Cria o token de acesso (JWT) para obter a autorização.")
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginUserRequest userRequest) throws Exception {
       var jwtToken = userService.login(userRequest);
       return new ResponseEntity<>(jwtToken, HttpStatus.OK);
       
    }

    @Operation(summary = "Realiza o logout do usuário", description = "Invalida o token de acesso (JWT) atual do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authorization) {
        String token = TokenUtils.recoveryToken(authorization);
        jwtTokenServiceImpl.invalidateToken(token);
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }

    @Operation(summary = "Atualiza o token de acesso", description = "Gera um novo token de acesso (JWT) usando um refresh token válido. O accessToken antigo e expirado deve ser enviado no cabeçalho de autorização.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Refresh token inválido ou expirado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtToken> refresh(
            @Parameter(hidden = true)
            @RequestHeader String Authorization,
            @RequestBody RefreshTokenRequest refreshToken) throws Exception {

        var newJwtToken = userService.useRefreshToken(TokenUtils.recoveryToken(Authorization), UUID.fromString(refreshToken.refreshToken()));
        return new ResponseEntity<>(newJwtToken, HttpStatus.OK);
    }

    @Operation(summary = "Endpoint de teste de autenticação", description = "Verifica se o token de acesso fornecido é válido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido e autenticado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/test-autenticated")
    public ResponseEntity<String> test(
            @Parameter(hidden = true)
            @RequestHeader String Authorization,
            @RequestBody String alow) throws Exception {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


}
