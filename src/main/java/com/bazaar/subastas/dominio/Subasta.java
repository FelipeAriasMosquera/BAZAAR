package com.bazaar.subastas.dominio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class Subasta {

    private Long id;
    private Long productoId;
    private Long vendedorId;
    private BigDecimal precioInicial;
    private BigDecimal precioActual;
    private BigDecimal incrementoMinimo;
    private BigDecimal precioReserva;   // secreto: nunca sale por la API
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFin;
    private EstadoSubasta estado;
    private Long ganadorId;             // líder actual; al cerrar, solo si superó la reserva

    public Puja registrarPuja(Long usuarioId, BigDecimal monto, OffsetDateTime ahora) {
        if (estado != EstadoSubasta.ACTIVA) {
            throw new PujaInvalidaException("La subasta no está activa");
        }
        // RF-15: el tiempo se valida en el servidor, aunque el scheduler aún no haya cerrado la subasta
        if (ahora.isBefore(fechaInicio) || ahora.isAfter(fechaFin)) {
            throw new PujaInvalidaException("La subasta está fuera de su ventana de tiempo");
        }
        if (usuarioId.equals(vendedorId)) {
            throw new PujaInvalidaException("El vendedor no puede pujar en su propia subasta");
        }
        if (usuarioId.equals(ganadorId)) {
            throw new PujaInvalidaException("Ya eres el líder de esta subasta");
        }
        BigDecimal minimo = (ganadorId == null) ? precioInicial : precioActual.add(incrementoMinimo);
        if (monto.compareTo(minimo) < 0) {
            throw new PujaInvalidaException("El monto debe ser al menos " + minimo);
        }

        this.precioActual = monto;
        this.ganadorId = usuarioId;
        return new Puja(null, id, usuarioId, monto, ahora);
    }

    /** PROGRAMADA -> ACTIVA. Devuelve true si cambió. */
    public boolean activar(OffsetDateTime ahora) {
        if (estado == EstadoSubasta.PROGRAMADA && !ahora.isBefore(fechaInicio)) {
            estado = EstadoSubasta.ACTIVA;
            return true;
        }
        return false;
    }

    /** ACTIVA -> FINALIZADA. Devuelve true si hay ganador válido (hubo pujas y superó la reserva). */
    public boolean cerrar(OffsetDateTime ahora) {
        if (estado != EstadoSubasta.ACTIVA || ahora.isBefore(fechaFin)) {
            return false;
        }
        estado = EstadoSubasta.FINALIZADA;
        boolean reservaCumplida = precioReserva == null || precioActual.compareTo(precioReserva) >= 0;
        if (ganadorId != null && reservaCumplida) {
            return true;
        }
        ganadorId = null;   // RF-19: finalizada sin ganador
        return false;
    }
}