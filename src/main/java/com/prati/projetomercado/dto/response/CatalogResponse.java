package com.prati.projetomercado.dto.response;

public record CatalogResponse(
        Long id,
        String CODE,
        String unit,
        String name,
        Long marketId
) {
}

