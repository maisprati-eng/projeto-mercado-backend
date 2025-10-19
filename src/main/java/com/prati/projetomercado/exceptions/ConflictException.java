package com.prati.projetomercado.exceptions;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conflito de estado da aplicação (ex.: e-mail já cadastrado).
 * Use para sinalizar 409 Conflict com mensagens de campo (fields).
 */
@Getter
public class ConflictException extends RuntimeException {
    private final Map<String, String> fieldErrors = new LinkedHashMap<>();

    public ConflictException(String message) {
        super(message);
    }

    /** Adiciona um erro específico de campo (ex.: email -> "já em uso"). */
    public ConflictException withField(String field, String message) {
        fieldErrors.put(field, message);
        return this;
    }

}
