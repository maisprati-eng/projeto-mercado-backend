package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.handlers.ResponseHandler;
import com.prati.projetomercado.dto.request.NfcePatchRequest;
import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.ErrorResponse;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.PageResponse;
import com.prati.projetomercado.dto.response.StatesResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.impl.NfceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nfces")
@RequiredArgsConstructor
@Tag(name = "NFC-e", description = "Endpoints para gerenciamento de Notas Fiscais de Consumidor Eletrônicas de usuários")
@SecurityRequirement(name = "bearerAuth")
public class NfceController {

    private final NfceServiceImpl nfceService;

    @Operation(summary = "Lista todas as notas fiscais",
            description = "Retorna uma lista de todas as notas fiscais do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notas fiscais retornada com sucesso")
    })
    @GetMapping()
    public ResponseEntity<ResponseHandler<List<NfceResponse>>> getAllNfces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NfceResponse> data = nfceService.findAllByUser(page, size);
        PageResponse pageInfo = PageResponse.from(data);
        return ResponseEntity.ok(ResponseHandler.pageableSuccess("Notas fiscais encontradas com sucesso", data.getContent(), pageInfo));
    }

    @Operation(summary = "Busca uma nota fiscal pela chave de acesso",
            description = "Retorna os detalhes de um nota fiscal específica do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota fiscal encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @GetMapping("/{access-key}")
    public ResponseEntity<ResponseHandler<NfceResponse>> getNfce(@PathVariable("access-key") String accessKey) {
        NfceResponse data = nfceService.findByAccessKey(accessKey);
        return ResponseEntity.ok(ResponseHandler.success("Nota fiscal encontrada com sucesso.", data));
    }

    @Operation(summary = "Lista todos os estados",
            description = "Retorna uma lista dos estados implementados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estados implementados retornada com sucesso")
    })
    @GetMapping("/states")
    public ResponseEntity<ResponseHandler<StatesResponse>> getAvailableStates() {
        StatesResponse data = nfceService.getAvailableStates();
        return ResponseEntity.ok(ResponseHandler.success("Lista de estados implementados encontrada com sucesso.", data));
    }

    @Operation(summary = "Cria uma nova nota fiscal pelo link",
            description = "Cria um nova nota fiscal pelo link associada ao usuário autenticado e retorna o objeto da nota fiscal criada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota fiscal criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição não pôde ser processada devido a URL inválida ou falha na extração de dados da página",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Supermercado não encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Nota fiscal já existe",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping("/from-url")
    public ResponseEntity<ResponseHandler<NfceResponse>> createNfceFromLink(@RequestBody UrlRequest request) {
        NfceResponse data = nfceService.createFromLink(request.getUrl());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseHandler.success("Nota fiscal pelo link cadastrada com sucesso.", data));
    }

    @Operation(summary = "Cria uma nova nota fiscal manual",
            description = "Cria um nova nota fiscal manual associada ao usuário autenticado e retorna o objeto da nota fiscal criada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota fiscal criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Supermercado não encontrado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Nota fiscal já existe",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping()
    public ResponseEntity<ResponseHandler<NfceResponse>> createNfceManually(@RequestBody @Valid NfceRequest manualData) {
        NfceResponse data = nfceService.createManually(manualData);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseHandler.success("Nota fiscal manual cadastrada com sucesso.", data));
    }

    @Operation(summary = "Atualiza uma nota fiscal",
            description = "Atualiza as informações de uma nota fiscal com base na chave de acesso e retorna o objeto da nota fiscal atualizada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota fiscal atualizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para editar esta nota fiscal",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation =
                                    ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Notas fiscais registradas por QR code não podem ser editadas",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PutMapping("/{access-key}")
    public ResponseEntity<ResponseHandler<NfceResponse>> updateNfce(@PathVariable("access-key") String accessKey, @RequestBody @Valid NfceRequest updatedNfce) {
        NfceResponse data = nfceService.update(accessKey, updatedNfce);
        return ResponseEntity.ok(ResponseHandler.success("Nota fiscal editada com sucesso.", data));
    }

    @Operation(summary = "Atualiza parcialmente uma nota fiscal",
            description = "Atualiza parcialmente os campos de uma nota fiscal com base na chave de acesso e retorna o objeto da nota fiscal atualizada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota fiscal atualizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para editar esta nota fiscal",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation =
                                    ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Notas fiscais registradas por QR code não podem ser editadas",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PatchMapping("/{access-key}")
    public ResponseEntity<ResponseHandler<NfceResponse>> partialUpdateNfce(@PathVariable("access-key") String accessKey, @RequestBody NfcePatchRequest patchNfce) {
        NfceResponse data = nfceService.partialUpdate(accessKey, patchNfce);
        return ResponseEntity.ok(ResponseHandler.success("Nota fiscal editada com sucesso.", data));
    }

    @Operation(summary = "Remove uma nota fiscal",
            description = "Remove permanentemente uma nota fiscal com base na chave de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota fiscal deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para deletar esta nota fiscal",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @DeleteMapping("/{access-key}")
    public ResponseEntity<ResponseHandler<Void>> deleteNfce(@PathVariable("access-key") String accessKey) {
        nfceService.delete(accessKey);
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
    }
}
