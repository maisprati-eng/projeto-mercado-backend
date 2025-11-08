package com.prati.projetomercado.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfceRequest(
        @NotNull(message="Nao pode ser nulo")
        SupermarketRequest supermarket,
        String accessKey,
        @NotNull(message="Nao pode ser nulo")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        BigDecimal totalPrice,
        List<Item> products
) {
    public record Item(
            String name,
            String code,
            BigDecimal quantity,
            String unit,
            BigDecimal price
    ) {
    }
}
