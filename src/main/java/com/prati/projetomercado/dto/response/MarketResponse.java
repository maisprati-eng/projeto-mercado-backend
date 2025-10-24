package com.prati.projetomercado.dto.response;

import java.util.List;

public record MarketResponse(
        Long id,
        String name,
        String city,
        String state,
        String cnpj
) {
}
