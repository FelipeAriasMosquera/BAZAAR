package com.bazaar.catalogo.entity;

public enum EstadoProducto {
    ACTIVO("activo"),
    PAUSADO("pausado"),
    VENDIDO("vendido"),
    ELIMINADO("eliminado");

    private final String valor;

    EstadoProducto(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoProducto desde(String valor) {
        for (EstadoProducto e : values()) {
            if (e.valor.equalsIgnoreCase(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("estado inválido: " + valor);
    }
}
