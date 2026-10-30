package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.common.exception.ErrorResponse;
import com.bazaar.subastas.dominio.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// HIGHEST_PRECEDENCE: si no, el handleGeneric(Exception) del GlobalExceptionHandler los convierte en 500
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = SubastaController.class)
public class SubastaExceptionHandler {

    @ExceptionHandler({PujaInvalidaException.class, SubastaInvalidaException.class})
    public ResponseEntity<ErrorResponse> invalida(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(SubastaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> noEncontrada(SubastaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage()));
    }

    // AccessDeniedException: lo lanza @PreAuthorize; sin este handler caería en handleGeneric (500)
    @ExceptionHandler({OperacionNoPermitidaException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> prohibido(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "Forbidden", "No tienes permiso para esta operación"));
    }
}