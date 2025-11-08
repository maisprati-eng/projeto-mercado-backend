package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.handlers.ResponseHandler;
import com.prati.projetomercado.dto.request.CreateRascunhoRequest;
import com.prati.projetomercado.dto.request.UpdateRascunhoRequest;
import com.prati.projetomercado.dto.response.PageResponse;
import com.prati.projetomercado.dto.response.RascunhoResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.RascunhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rascunhos") // Define o caminho base para todos os endpoints neste controller
@RequiredArgsConstructor
@Tag(name = "Rascunhos", description = "Endpoints para gerenciamento de rascunhos de usuários")
@SecurityRequirement(name = "bearerAuth") // Exige autenticação para TODOS os endpoints neste controller
public class RascunhoController {

    // Injeção de dependência do nosso service
    private final RascunhoService rascunhoService;

    @PostMapping
    @Operation(summary = "Cria um novo rascunho", description = "Cria um novo rascunho associado ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rascunho criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<ResponseHandler<RascunhoResponse>> criarRascunho(@RequestBody CreateRascunhoRequest request) {
        RascunhoResponse response = rascunhoService.criarRascunho(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success("Rascunho criado com sucesso.", response));
    }

    @GetMapping
    @Operation(summary = "Lista todos os rascunhos do usuário", description = "Retorna uma lista de todos os rascunhos pertencentes ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de rascunhos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<ResponseHandler<List<RascunhoResponse>>> buscarRascunhosDoUsuario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RascunhoResponse> response = rascunhoService.buscarRascunhosDoUsuario(page, size);
        PageResponse pageInfo = PageResponse.from(response);
        return ResponseEntity.ok(ResponseHandler.pageableSuccess("Rascunhos encontrados com sucesso.", response.getContent(), pageInfo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um rascunho por ID", description = "Retorna os detalhes de um rascunho específico, se pertencer ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho encontrado"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<ResponseHandler<RascunhoResponse>> buscarRascunhoPorId(@PathVariable("id") Long rascunhoId) {
        RascunhoResponse response = rascunhoService.buscarRascunhoPorId(rascunhoId);
        return ResponseEntity.ok(ResponseHandler.success("Rascunho encontrado com sucesso.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um rascunho", description = "Atualiza o título e o conteúdo de um rascunho existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<ResponseHandler<RascunhoResponse>> atualizarRascunho(@PathVariable("id") Long rascunhoId, @RequestBody UpdateRascunhoRequest request) {
        RascunhoResponse response = rascunhoService.atualizarRascunho(rascunhoId, request);
        return ResponseEntity.ok(ResponseHandler.success("Rascunho editado com sucesso.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apaga um rascunho", description = "Remove um rascunho permanentemente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rascunho apagado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<Void> apagarRascunho(@PathVariable("id") Long rascunhoId) {
        rascunhoService.apagarRascunho(rascunhoId);
        return ResponseEntity.noContent().build();
    }
}
