package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.handlers.ResponseHandler;
import com.prati.projetomercado.dto.response.CatalogResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.CatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catalogo", description = "Rotas de manipulação do catálogo dos mercados cadastrados")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/{id}")
    public ResponseEntity<com.prati.projetomercado.dto.handlers.SuccessResponse<List<CatalogResponse>>> getCatalogByMarket(@PathVariable("id") Long id) {
        var response = catalogService.getCatalogByMarket(id);
        return ResponseEntity.ok(ResponseHandler.success("Catalogo retornado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseHandler<Void>> deleteCatalog(@PathVariable("id") Long id) {
        catalogService.deleteCatalog(id);
        return ResponseEntity.ok(ResponseHandler.success("item do catalogo deletado com sucesso!", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseHandler<CatalogResponse>> editCatalogItem(@PathVariable("id") Long id, @RequestBody NameRequest request) {
        var response = catalogService.editCatalog(id, request.getName());
        return ResponseEntity.ok(ResponseHandler.success("Catalogo editado com sucesso", response));
    }

    @Getter
    @Setter
    public static class NameRequest {
        private String name;
    }
}
