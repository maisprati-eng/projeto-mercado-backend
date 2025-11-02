package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.response.UserResponse;
import com.prati.projetomercado.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.prati.projetomercado.dto.request.ChangePasswordRequest;
import org.springframework.web.bind.annotation.*;
import com.prati.projetomercado.dto.request.SearchRequestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


@RestController
@RequestMapping("/api/user")// Define o caminho base para todos os endpoints de usuário
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Endpoints para gerenciamento de informações do usuário")
public class UserController {
    private final UserService userService; // Injeta o serviço

    @GetMapping
    @Operation(summary = "Retorna informações do usuário autenticado", description = "Busca e retorna os dados públicos do usuário que está logado, com base no token de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado, token inválido ou ausente")
    })
    @SecurityRequirement(name = "bearerAuth") // Marca este endpoint como protegido no Swagger
    public ResponseEntity<UserResponse> getUserInfo() {
        // Chama o método do serviço que implementamos
        UserResponse userInfo = userService.getUserInfo();
        // Retorna os dados com um status 200 OK
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Altera a senha do usuário autenticado", description = "Permite que o usuário logado altere sua própria senha fornecendo a senha atual e a nova senha.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: senha atual incorreta, nova senha não confere)"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Senha alterada com sucesso.");
    }

    @PostMapping("/search")
    @Operation(summary = "Busca usuários dinamicamente",
            description = "Realiza uma busca avançada por usuários com base nos filtros fornecidos.")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestBody SearchRequestDTO searchRequest) {
        List<UserResponse> users = userService.searchUsers(searchRequest);
        return ResponseEntity.ok(users);
    }

}
