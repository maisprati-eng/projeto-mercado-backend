package com.prati.projetomercado.exceptions;

import java.util.List;

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

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
