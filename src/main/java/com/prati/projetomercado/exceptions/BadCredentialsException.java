package com.prati.projetomercado.exceptions;

import java.util.List;

public class BadCredentialsException extends RuntimeException {
    public List<FieldError> fieldErrors;
    public BadCredentialsException(List<FieldError> fieldErrors) {
        super("Bad credentials");

        this.fieldErrors = fieldErrors;
    }
}
