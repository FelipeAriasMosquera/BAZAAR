package com.bazaar.pagos.adaptadores.entrada.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PagoResponse(
        Long id,
        Long ordenId,
        String metodo,
        BigDecimal monto,
        String estado,
        String referenciaPasarela,
        Instant fecha
) {
}
