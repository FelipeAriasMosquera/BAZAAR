package com.bazaar.subastas.dominio;

public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String mensaje) { super(mensaje); }
}