package com.prati.projetomercado.dto.response;

public record ErrorResponse(
        String statusMessage,
        boolean success
) {
    public ErrorResponse(String statusMessage) {
        this(statusMessage, false);
    }
}
