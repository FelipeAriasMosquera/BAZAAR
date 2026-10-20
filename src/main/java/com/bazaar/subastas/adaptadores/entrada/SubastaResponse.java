package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.subastas.dominio.Subasta;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

// A propósito NO incluye precioReserva: debe ser invisible para los compradores
public record SubastaResponse(Long id, Long productoId, BigDecimal precioActual, BigDecimal incrementoMinimo,
                              OffsetDateTime fechaInicio, OffsetDateTime fechaFin, String estado, Long liderId) {

    public static SubastaResponse de(Subasta s) {
        return new SubastaResponse(s.getId(), s.getProductoId(), s.getPrecioActual(), s.getIncrementoMinimo(),
                s.getFechaInicio(), s.getFechaFin(), s.getEstado().name(), s.getGanadorId());
    }
}