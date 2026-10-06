package com.bazaar.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(

        @NotNull(message = "El vendedor es obligatorio")
        Long vendedorId,

        Long categoriaId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
        String nombre,

        String slug,

        String descripcion,

        @NotBlank(message = "El tipo de venta es obligatorio ('fijo' o 'subasta')")
        String tipoVenta,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Valid
        List<ProductoImagenRequest> imagenes
) {}
