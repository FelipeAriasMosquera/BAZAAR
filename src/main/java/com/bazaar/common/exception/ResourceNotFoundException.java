package com.bazaar.common.exception;

/** Usar cuando un recurso solicitado por id/slug no existe. Se traduce a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
