package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.subastas.dominio.PujaDetalle;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PujaResponse(Long id, String postor, BigDecimal monto, OffsetDateTime fecha, boolean esMia) {

    public static PujaResponse de(PujaDetalle d, Long solicitanteId) {
        return new PujaResponse(d.id(), alias(d.nombre()), d.monto(), d.fecha(),
                d.usuarioId().equals(solicitanteId));
    }

    private static String alias(String nombre) {
        if (nombre == null || nombre.isBlank()) return "Usuario";
        String[] partes = nombre.trim().split("\\s+");
        return partes.length > 1 ? partes[0] + " " + partes[1].charAt(0) + "." : partes[0];
    }
}