package com.bazaar.pagos.dominio;

import java.math.BigDecimal;

/**
 * Lo que el dominio le pide a la pasarela. pagoId sirve como clave de idempotencia:
 * una pasarela real no debe cobrar dos veces la misma solicitud si reintentamos.
 */
public record SolicitudCobro(Long pagoId, Long ordenId, BigDecimal monto, MetodoPago metodo) {
}
