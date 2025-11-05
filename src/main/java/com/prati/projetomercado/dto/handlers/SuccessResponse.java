package com.prati.projetomercado.dto.handlers;

public record SuccessResponse<T>(
        String status,
        String message,
        T data
) implements ResponseHandler<T> {
}
