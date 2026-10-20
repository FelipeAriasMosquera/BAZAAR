package com.bazaar.subastas.dominio;

public class PujaInvalidaException extends RuntimeException {
    public PujaInvalidaException(String mensaje) { super(mensaje); }
}