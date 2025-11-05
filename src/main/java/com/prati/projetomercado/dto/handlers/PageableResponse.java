package com.prati.projetomercado.dto.handlers;

import com.prati.projetomercado.dto.response.PageResponse;

import java.util.List;

public record PageableResponse<T>(
        String status,
        String message,
        List<T> data,
        PageResponse page
) implements ResponseHandler<T> {
}
