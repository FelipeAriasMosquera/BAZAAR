package com.bazaar.catalogo.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductoResponse(
        Long id,
        Long vendedorId,
        Long categoriaId,
        String categoriaNombre,
        String nombre,
        String slug,
        String descripcion,
        String tipoVenta,
        BigDecimal precio,
        Integer stock,
        String estado,
        Instant fechaCreacion,
        List<ProductoImagenResponse> imagenes
) {}
