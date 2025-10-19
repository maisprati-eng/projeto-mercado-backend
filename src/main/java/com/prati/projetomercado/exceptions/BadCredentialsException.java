package com.prati.projetomercado.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class BadCredentialsException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public BadCredentialsException(List<FieldError> fieldErrors) {
        super("Credenciais inválidas");
        this.fieldErrors = fieldErrors;
    }

}
