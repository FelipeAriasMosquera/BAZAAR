package com.bazaar.subastas.dominio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Puja(Long id, Long subastaId, Long usuarioId, BigDecimal monto, OffsetDateTime fecha) {}
