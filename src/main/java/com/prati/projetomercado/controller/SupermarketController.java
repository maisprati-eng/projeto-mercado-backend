package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.handlers.ResponseHandler;
import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.ErrorResponse;
import com.prati.projetomercado.dto.response.PageResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.service.impl.SupermarketServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supermarkets")
@RequiredArgsConstructor
@Tag(name = "Supermercados", description = "Endpoints para gerenciamento de supermercados de usuários")
@SecurityRequirement(name = "bearerAuth")
public class SupermarketController {

    private final SupermarketServiceImpl marketService;

    @Operation(summary = "Lista todos os supermercados",
            description = "Retorna uma lista de todos os supermercados do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de supermercados retornada com sucesso")
    })
    @GetMapping()
    public ResponseEntity<ResponseHandler<List<SupermarketResponse>>> getAllSupermarkets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SupermarketResponse> data = marketService.findAllByUser(page, size);
        PageResponse pageInfo = PageResponse.from(data);
        return ResponseEntity.ok(ResponseHandler.pageableSuccess("Supermercados encontrados com sucesso.", data.getContent(), pageInfo));
    }

    @Operation(summary = "Busca um supermercado pelo ID",
            description = "Retorna os detalhes de um supermercado específico do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supermercado encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Supermercado não encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHandler<SupermarketResponse>> getSupermarket(@PathVariable long id) {
        SupermarketResponse data = marketService.findById(id);
        return ResponseEntity.ok(ResponseHandler.success("Supermercado encontrado com sucesso.", data));
    }

    @Operation(summary = "Cria um novo supermercado",
            description = "Cria um novo supermercado associado ao usuário autenticado e retorna o objeto do supermercado criado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Supermercado criado com sucesso")
    })
    @PostMapping()
    public ResponseEntity<ResponseHandler<SupermarketResponse>> createSupermarket(@RequestBody SupermarketRequest supermarketData) {
        SupermarketResponse data = marketService.create(supermarketData);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseHandler.success("Supermercado cadastrado com sucesso.", data));

    }

    @Operation(summary = "Atualiza um supermercado",
            description = "Atualiza as informações de um supermercado com base no ID e retorna o objeto do supermercado atualizado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supermercado atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para editar este supermercado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Supermercado não encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation =
                                    ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Supermercados registrados por QR code não podem ser editados",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseHandler<SupermarketResponse>> updateSupermarket(@PathVariable long id, @RequestBody SupermarketRequest supermarketData) {
        SupermarketResponse data = marketService.update(id, supermarketData);

        return ResponseEntity.ok(ResponseHandler.success("Supermercado editado com sucesso.", data));
    }

    @Operation(summary = "Remove um supermercado",
            description = "Remove permanentemente um supermercado com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Supermercado deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar este supermercado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Supermercado não encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Supermercados registrados por QR code não podem ser deletados; ou existem notas fiscais associadas ao supermercado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteSupermarket(@PathVariable long id) {
        marketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
