package com.prati.projetomercado.controller;


import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.request.RefreshTokenRequest;
import com.prati.projetomercado.dto.response.AuthResponse;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.service.UserService;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // MELHOR PRÁTICA: Para injeção de dependências
@Tag(name = "Autenticação", description = "Endpoints para registro, login e gerenciamento de tokens")
public class AuthController {

    // MELHOR PRÁTICA: Injetando dependências com 'final'
    private final UserService userService;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Operation(summary = "Registra um novo usuário", description = "Cria uma nova conta de usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso (ou e-mail de confirmação enviado)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: senhas não conferem)")
    })
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserRequest userRequest) {
        // A validação de senhas agora está corretamente no Service, mas podemos manter uma aqui se quisermos.
        // Por consistência, vamos confiar na lógica do Service.
        userService.registerUser(userRequest);
        return ResponseEntity.ok("Solicitação de registro processada com sucesso!");
    }


    @Operation(summary = "Realiza o login de um usuário", description = "Autentica um usuário, retornando um token de acesso e um refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta não confirmada")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginUserRequest userRequest) throws Exception {
        var authResponse= userService.login(userRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Operation(summary = "Realiza o logout do usuário", description = "Invalida o token de acesso (JWT) atual do usuário, apagando-o do banco de dados.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")})
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authorization) {
        String token = TokenUtils.recoveryToken(authorization);
        jwtTokenServiceImpl.invalidateToken(token); // Este método agora apaga o token do DB
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }


    @Operation(summary = "Atualiza o token de acesso", description = "Gera um novo token de acesso (JWT) usando um refresh token válido.")
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

    @GetMapping("/confirm-registration")
    @Operation(summary = "Confirma o registro de um novo usuário", description = "Endpoint ativado pelo link enviado ao e-mail do usuário para validar a conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        try {
            userService.confirmUser(token);
            String htmlBody = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>"
                    + "<h1>✅ Conta Ativada com Sucesso!</h1>"
                    + "<p>Sua conta foi verificada. Você já pode fechar esta aba e fazer o login na aplicação.</p>"
                    + "</body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlBody);
        } catch (AuthException e) {
            String errorHtmlBody = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px; color: #D8000C;'>"
                    + "<h1>❌ Erro na Ativação</h1>"
                    + "<p>Ocorreu um erro: " + e.getMessage() + "</p>"
                    + "<p>O link pode ser inválido ou ter expirado. Por favor, tente se registrar novamente.</p>"
                    + "</body></html>";
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(errorHtmlBody);
        }
    }
}
