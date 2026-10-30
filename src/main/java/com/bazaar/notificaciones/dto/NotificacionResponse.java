package com.bazaar.notificaciones.dto;

import java.time.Instant;

public record NotificacionResponse(
        Long id,
        String tipo,
        String mensaje,
        boolean leido,
        Instant fecha
) {}
