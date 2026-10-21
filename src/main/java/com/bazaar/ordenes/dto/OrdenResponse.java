package com.bazaar.ordenes.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrdenResponse(
        Long id,
        Long usuarioId,
        Long direccionId,
        String estado,
        BigDecimal subtotal,
        BigDecimal envio,
        BigDecimal total,
        Instant fecha,
        List<OrdenItemResponse> items
) {}
