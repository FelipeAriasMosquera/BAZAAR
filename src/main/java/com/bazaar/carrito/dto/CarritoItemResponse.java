package com.bazaar.carrito.dto;

import java.math.BigDecimal;

public record CarritoItemResponse(
        Long id,
        Long productoId,
        String productoNombre,
        String productoSlug,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal
) {}
