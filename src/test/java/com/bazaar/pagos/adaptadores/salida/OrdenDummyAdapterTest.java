package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.dominio.OrdenParaPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenDummyAdapterTest {

    private final OrdenDummyAdapter ordenes = new OrdenDummyAdapter();

    @Test
    void lasCuatroOrdenesDummyExistenConLosTotalesDelSql() {
        assertEquals(new BigDecimal("150000.00"), ordenes.buscarPorId(1001L).orElseThrow().total());
        assertEquals(new BigDecimal("89900.50"), ordenes.buscarPorId(1002L).orElseThrow().total());
        assertEquals(new BigDecimal("230000.00"), ordenes.buscarPorId(1003L).orElseThrow().total());
        assertEquals(new BigDecimal("7500000.00"), ordenes.buscarPorId(1004L).orElseThrow().total());
    }

    @Test
    void laOrden1003YaEstaPagadaYLasDemasPendientes() {
        assertTrue(ordenes.buscarPorId(1001L).orElseThrow().pendienteDePago());
        assertTrue(ordenes.buscarPorId(1002L).orElseThrow().pendienteDePago());
        assertFalse(ordenes.buscarPorId(1003L).orElseThrow().pendienteDePago());
        assertTrue(ordenes.buscarPorId(1004L).orElseThrow().pendienteDePago());
    }

    @Test
    void marcarComoPagadaDejaLaOrdenSinPendienteDePago() {
        ordenes.marcarComoPagada(1001L);

        OrdenParaPago orden = ordenes.buscarPorId(1001L).orElseThrow();
        assertFalse(orden.pendienteDePago());
        assertEquals(new BigDecimal("150000.00"), orden.total());
    }

    @Test
    void unaOrdenInexistenteDevuelveVacioYNoSePuedeMarcarComoPagada() {
        assertTrue(ordenes.buscarPorId(9999L).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> ordenes.marcarComoPagada(9999L));
    }
}
