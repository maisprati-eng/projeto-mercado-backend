package com.prati.projetomercado.dto.handlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prati.projetomercado.dto.response.PageResponse;
import com.prati.projetomercado.exceptions.FieldError;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface ResponseHandler<T> permits ErrorResponse, SuccessResponse, PageableResponse, ValidationErrorResponse {

    final String errorStatus = "error";
    final String defaultError = "defaultError";
    final String fieldError = "fieldError";


    static <T> SuccessResponse<T> success(String message) {
        return new SuccessResponse<>("success", message, null);
    }

    static <T> SuccessResponse<T> success(String message, T data) {
        return new SuccessResponse<>("success", message, data);
    }

    static <T> PageableResponse<List<T>> pageableSuccess(String message, List<T> data, PageResponse pageableResponse) {
        return new PageableResponse<>("success", message, Collections.singletonList(data), pageableResponse);
    }

    static <T> ErrorResponse<T> error(String message) {
        return new ErrorResponse<>(errorStatus, defaultError, message);
    }

    static <T> ValidationErrorResponse<T> validationError(List<FieldError> errorList) {
        return new ValidationErrorResponse<>(
                errorStatus,
                fieldError,
                errorList.stream().collect(Collectors.toMap(FieldError::fieldName, FieldError::errorMessage))
        );
    }


}
