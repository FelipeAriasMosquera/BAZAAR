package com.bazaar.subastas.dominio;

public class SubastaInvalidaException extends RuntimeException {
    public SubastaInvalidaException(String mensaje) { super(mensaje); }
}