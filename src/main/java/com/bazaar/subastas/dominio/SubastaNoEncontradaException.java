package com.bazaar.subastas.dominio;

public class SubastaNoEncontradaException extends RuntimeException {
    public SubastaNoEncontradaException(Long id) { super("Subasta " + id + " no encontrada"); }
}