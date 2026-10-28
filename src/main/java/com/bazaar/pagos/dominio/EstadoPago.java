package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;

/** Estados del pago. En BD se guardan en minúscula (ver el CHECK de la tabla pagos). */
public enum EstadoPago {
    PENDIENTE("pendiente"),
    APROBADO("aprobado"),
    RECHAZADO("rechazado"),
    REEMBOLSO_EN_PROCESO("reembolso_en_proceso"),
    REEMBOLSADO("reembolsado");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoPago desde(String valor) {
        for (EstadoPago estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new BusinessException("Estado de pago desconocido: " + valor);
    }
}
