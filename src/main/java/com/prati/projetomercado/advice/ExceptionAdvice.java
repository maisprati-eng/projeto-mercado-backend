package com.prati.projetomercado.advice;

import com.prati.projetomercado.dto.handlers.ResponseHandler;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.DuplicateEntityException;
import com.prati.projetomercado.exceptions.EntityDeletionException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.NfceScrapeException;
import com.prati.projetomercado.exceptions.NfceUrlParseException;
import com.prati.projetomercado.exceptions.NotManualEntityException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.prati.projetomercado.exceptions.EmailAlreadyExistsException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseHandler<Void>> handleException(Exception ex) {
        return ResponseEntity.status(500).body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseHandler<Void>> handleRuntimeException(Exception ex) {
        return ResponseEntity.status(500).body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ResponseHandler<Void>> handleAuthException(AuthException ex) {
        return ResponseEntity.status(401).body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseHandler<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(ResponseHandler.validationError(ex.fieldErrors));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ResponseHandler<Void>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseHandler<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler({
            DuplicateEntityException.class,
            EntityDeletionException.class,
            NotManualEntityException.class
    })
    public ResponseEntity<ResponseHandler<Void>> handleEntityConflictExceptions(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler({
            NfceScrapeException.class,
            NfceUrlParseException.class
    })
    public ResponseEntity<ResponseHandler<Void>> handleNfceBadRequestExceptions(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseHandler.error(ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseHandler<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseHandler.error(ex.getMessage()));
    }
}
