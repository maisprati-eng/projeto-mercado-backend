package com.prati.projetomercado.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class BadRequestException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public BadRequestException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public BadRequestException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

}
