package com.bazaar.notificaciones.entity;

/**
 * Tipos de aviso. La BD guarda el nombre tal cual (VARCHAR(40), sin CHECK),
 * así que agregar un tipo nuevo solo requiere agregarlo aquí.
 */
public enum TipoNotificacion {
    PUJA_SUPERADA,      // RF-21
    SUBASTA_GANADA,     // RF-22
    ORDEN_PAGADA,
    ORDEN_ENVIADA,
    ORDEN_ENTREGADA,
    GENERAL
}
