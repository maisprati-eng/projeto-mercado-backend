package com.prati.projetomercado.dto.response;

import com.prati.projetomercado.exceptions.FieldError;

import java.util.List;

public record BadCredentialsResponse(String message, List<FieldError> fieldErrosList) {
}
