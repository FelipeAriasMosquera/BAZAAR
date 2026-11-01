package com.bazaar.notificaciones.mapper;

import com.bazaar.notificaciones.dto.NotificacionResponse;
import com.bazaar.notificaciones.entity.Notificacion;

import java.util.Locale;

public class NotificacionMapper {

    private NotificacionMapper() {}

    public static NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
                n.getId(),
                n.getTipo().name().toLowerCase(Locale.ROOT),
                n.getMensaje(),
                n.isLeido(),
                n.getFecha()
        );
    }
}
