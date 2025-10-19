package com.prati.projetomercado.advice;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.BadRequestException;
import com.prati.projetomercado.exceptions.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionAdvice {

    static class ErrorResponse {
        public Instant timestamp;
        public int status;
        public String error;
        public String message;
        public String path;

        // campos extras (ex.: "password": "mensagem") aparecem no topo do JSON
        private final Map<String, Object> extras = new LinkedHashMap<>();

        ErrorResponse(HttpStatus status, String message, String path) {
            this.timestamp = Instant.now();
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.path = path;
        }

        ErrorResponse withFields(List<FieldError> fieldErrors) {
            if (fieldErrors != null) {
                for (FieldError fe : fieldErrors) {
                    // cada erro vira uma chave no nível raiz do JSON
                    extras.put(fe.fieldName(), fe.errorMessage());
                }
            }
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extras;
        }
    }

    /* 404 - rota não existe */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        var body = new ErrorResponse(HttpStatus.NOT_FOUND, "Rota não encontrada", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /* 405 - método não permitido */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {

        String allowed = (ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty())
                ? " Permitidos: " + ex.getSupportedHttpMethods()
                : "";

        var body = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método não suportado." + allowed,
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    /* 400 - JSON malformado (body inválido) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        var body = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "JSON malformado ou tipo incompatível.",
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /* 400 - sua BadRequestException com erros de campo no topo */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        var body = new ErrorResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", req.getRequestURI())
                .withFields(ex.getFieldErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /* 400 - credenciais/validações */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        var status = HttpStatus.BAD_REQUEST; // mantenha 400 para alinhar com os testes/contrato
        var body = new ErrorResponse(status, "Credenciais inválidas", req.getRequestURI())
                .withFields(ex.getFieldErrors());
        return ResponseEntity.status(status).body(body);
    }

    /* 500 - fallback */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        var body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
