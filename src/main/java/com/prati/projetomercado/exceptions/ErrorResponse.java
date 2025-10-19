package com.prati.projetomercado.exceptions;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * Modelo único de resposta de erro da API.
 * Usado tanto pelo @RestControllerAdvice quanto pelo AuthenticationEntryPoint.
 *
 * Campos:
 * - timestamp: quando o erro aconteceu (UTC)
 * - status: código HTTP (ex.: 400, 401, 403, 500)
 * - error: motivo padrão do HTTP (ex.: "Bad Request", "Unauthorized")
 * - message: mensagem de negócio/técnica (ex.: "Token inválido")
 * - path: URI da requisição que gerou o erro
 */
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    /**
     * Construtor “completo” — use quando você tem o request em mãos (Advice).
     */
    public ErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        this.timestamp = Instant.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = request != null ? request.getRequestURI() : null;
    }

    /**
     * Construtor alternativo — use quando você só tem a path (ex.: SecurityEntryPoint).
     */
    public ErrorResponse(HttpStatus status, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    /**
     * Construtor simples — mantém compatibilidade com usos antigos.
     * Assume BAD_REQUEST e sem path.
     */
    public ErrorResponse(String message) {
        this.timestamp = Instant.now();
        this.status = HttpStatus.BAD_REQUEST.value();
        this.error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        this.message = message;
        this.path = null;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public static ErrorResponse of(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(status, message, request);
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(status, message, path);
    }
}
