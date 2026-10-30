package com.bazaar.subastas.dominio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PujaDetalle(Long id, Long usuarioId, String nombre, BigDecimal monto, OffsetDateTime fecha) {}