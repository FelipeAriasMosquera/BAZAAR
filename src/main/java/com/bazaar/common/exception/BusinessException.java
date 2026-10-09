package com.bazaar.common.exception;

/** Usar cuando se viola una regla de negocio (duplicados, referencias circulares, etc.). Se traduce a HTTP 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
