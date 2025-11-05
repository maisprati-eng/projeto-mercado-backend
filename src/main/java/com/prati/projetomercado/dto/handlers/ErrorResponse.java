package com.prati.projetomercado.dto.handlers;

public record ErrorResponse<T>(
        String status,
        String type,
        String message
) implements ResponseHandler<T> {
}
