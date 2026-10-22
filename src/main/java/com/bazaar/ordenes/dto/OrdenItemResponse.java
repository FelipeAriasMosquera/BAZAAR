package com.bazaar.ordenes.dto;

import java.math.BigDecimal;

public record OrdenItemResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Long subastaId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
