package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.common.exception.ErrorResponse;
import com.bazaar.subastas.dominio.PujaInvalidaException;
import com.bazaar.subastas.dominio.SubastaNoEncontradaException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// HIGHEST_PRECEDENCE: si no, el handleGeneric(Exception) del GlobalExceptionHandler lo convierte en 500
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = SubastaController.class)
public class SubastaExceptionHandler {

    @ExceptionHandler(PujaInvalidaException.class)
    public ResponseEntity<ErrorResponse> pujaInvalida(PujaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(SubastaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> noEncontrada(SubastaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage()));
    }
}