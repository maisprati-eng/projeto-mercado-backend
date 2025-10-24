package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.response.CatalogResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.CatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catalogo", description = "Rotas de manipulação do catálogo dos mercados cadastrados")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/{id}")
    public SuccessResponse<List<CatalogResponse>> getCatalogByMarket(@PathVariable("id") Long id) {
        return new SuccessResponse<>("Catalogo retornado com sucesso", catalogService.getCatalogByMarket(id));
    }

    @DeleteMapping("/{id}j")
    public SuccessResponse<Void> deleteCatalog(@PathVariable("id") Long id) {
        return new SuccessResponse<>("Item do catalogo deletado com sucesso!", null);
    }

    @PutMapping("/{id}")
    public SuccessResponse<CatalogResponse> editCatalogItem(@PathVariable("id") Long id, String name) {
        return new SuccessResponse<>("Catalogo editado com sucesso", catalogService.editCatalog(id, name));
    }

}
