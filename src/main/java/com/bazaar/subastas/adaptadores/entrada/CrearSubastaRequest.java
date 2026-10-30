package com.bazaar.subastas.adaptadores.entrada;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CrearSubastaRequest(
        @NotNull Long productoId,
        @NotNull @Positive BigDecimal precioInicial,
        @NotNull @Positive BigDecimal incrementoMinimo,
        @Positive BigDecimal precioReserva,          // opcional
        @NotNull OffsetDateTime fechaInicio,
        @NotNull OffsetDateTime fechaFin) {}