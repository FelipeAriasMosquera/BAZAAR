package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Métodos de pago aceptados. En BD: tarjeta, PSE, wallet (columna pagos.metodo). */
public enum MetodoPago {
    TARJETA("tarjeta"),
    PSE("PSE"),
    WALLET("wallet");

    private final String valor;

    MetodoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static MetodoPago desde(String valor) {
        for (MetodoPago metodo : values()) {
            if (metodo.valor.equalsIgnoreCase(valor)) {
                return metodo;
            }
        }
        String aceptados = Arrays.stream(values()).map(MetodoPago::getValor).collect(Collectors.joining(", "));
        throw new BusinessException("Método de pago no soportado: '" + valor + "'. Use: " + aceptados);
    }
}
