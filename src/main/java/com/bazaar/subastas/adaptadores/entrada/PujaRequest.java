package com.bazaar.subastas.adaptadores.entrada;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PujaRequest(@NotNull @Positive BigDecimal monto) {}