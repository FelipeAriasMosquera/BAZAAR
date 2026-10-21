package com.bazaar.ordenes.entity;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Ciclo de vida de una orden. La BD guarda estos valores en minúscula
 * (CHECK del schema), por eso existe EstadoOrdenConverter.
 *
 * Transiciones permitidas:
 *   PENDIENTE -> PAGADA | CANCELADA
 *   PAGADA    -> ENVIADA
 *   ENVIADA   -> ENTREGADA
 * Cancelar una orden ya pagada requiere reembolso (módulo pagos), por eso
 * no está permitido desde aquí.
 */
public enum EstadoOrden {
    PENDIENTE,
    PAGADA,
    ENVIADA,
    ENTREGADA,
    CANCELADA;

    public boolean permiteTransicionA(EstadoOrden destino) {
        return destinosPermitidos().contains(destino);
    }

    public String enBd() {
        return name().toLowerCase(Locale.ROOT);
    }

    private Set<EstadoOrden> destinosPermitidos() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(PAGADA, CANCELADA);
            case PAGADA -> EnumSet.of(ENVIADA);
            case ENVIADA -> EnumSet.of(ENTREGADA);
            case ENTREGADA, CANCELADA -> EnumSet.noneOf(EstadoOrden.class);
        };
    }
}
