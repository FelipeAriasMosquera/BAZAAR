package com.bazaar.pagos.adaptadores.entrada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * No lleva monto a propósito: el monto lo toma el sistema de la orden.
 * metodo acepta: tarjeta, PSE, wallet (sin distinguir mayúsculas).
 */
public record PagoRequest(
        @NotNull(message = "ordenId es obligatorio")
        @Positive(message = "ordenId debe ser positivo")
        Long ordenId,

        @NotBlank(message = "metodo es obligatorio")
        String metodo
) {
}
