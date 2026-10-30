package com.bazaar.resenas.dto;

import java.math.BigDecimal;

/** RF-33: promedio con un decimal (0.0 si todavía no hay reseñas) y cantidad de reseñas. */
public record ResumenResenasResponse(
        Long productoId,
        BigDecimal promedio,
        long total
) {}
