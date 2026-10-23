package com.bazaar.subastas.dominio;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubastaTest {

    private Subasta activa() {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new Subasta(1L, 10L, 99L, new BigDecimal("100000"), new BigDecimal("100000"),
                new BigDecimal("5000"), new BigDecimal("150000"),
                ahora.minusHours(1), ahora.plusDays(1), EstadoSubasta.ACTIVA, null);
    }

    @Test
    void primeraPujaPuedeIgualarElPrecioInicial() {
        Subasta s = activa();
        s.registrarPuja(1L, new BigDecimal("100000"), OffsetDateTime.now());
        assertEquals(1L, s.getGanadorId());
    }

    @Test
    void rechazaPujaMenorAlIncremento() {   // criterio de aceptación del RF-14
        Subasta s = activa();
        s.registrarPuja(1L, new BigDecimal("100000"), OffsetDateTime.now());
        assertThrows(PujaInvalidaException.class,
                () -> s.registrarPuja(2L, new BigDecimal("104000"), OffsetDateTime.now()));
        assertDoesNotThrow(() -> s.registrarPuja(2L, new BigDecimal("105000"), OffsetDateTime.now()));
    }

    @Test
    void vendedorNoPuedePujar() {
        assertThrows(PujaInvalidaException.class,
                () -> activa().registrarPuja(99L, new BigDecimal("100000"), OffsetDateTime.now()));
    }

    @Test
    void sinReservaCumplidaNoHayGanador() {
        Subasta s = activa();
        s.registrarPuja(1L, new BigDecimal("120000"), OffsetDateTime.now());
        assertFalse(s.cerrar(OffsetDateTime.now().plusDays(2)));
        assertNull(s.getGanadorId());
    }
}