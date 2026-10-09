package com.bazaar.catalogo.entity;

public enum TipoVenta {
    FIJO("fijo"),
    SUBASTA("subasta");

    private final String valor;

    TipoVenta(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoVenta desde(String valor) {
        for (TipoVenta t : values()) {
            if (t.valor.equalsIgnoreCase(valor)) {
                return t;
            }
        }
        throw new IllegalArgumentException("tipo_venta inválido: " + valor);
    }
}
