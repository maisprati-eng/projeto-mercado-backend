package com.prati.projetomercado.dto.handlers;

import java.util.Map;

public record ValidationErrorResponse<T>(
        String status,
        String type,
        Map<String, String> errorFields
) implements ResponseHandler<T> {
}
