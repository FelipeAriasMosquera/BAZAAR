package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagoTest {

    private Pago nuevoPago() {
        return Pago.nuevo(1001L, MetodoPago.TARJETA, new BigDecimal("150000"));
    }

    @Test
    void unPagoNuevoNaceSinIdPendienteYConMontoConDosDecimales() {
        Pago pago = nuevoPago();

        assertNull(pago.getId());
        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        assertEquals(new BigDecimal("150000.00"), pago.getMonto());
        assertNull(pago.getReferenciaPasarela());
        assertNotNull(pago.getFecha());
    }

    @Test
    void noSePuedeCrearUnPagoConMontoCeroNegativoONulo() {
        assertThrows(BusinessException.class, () -> Pago.nuevo(1L, MetodoPago.PSE, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> Pago.nuevo(1L, MetodoPago.PSE, new BigDecimal("-5")));
        assertThrows(BusinessException.class, () -> Pago.nuevo(1L, MetodoPago.PSE, null));
    }

    @Test
    void noSePuedeCrearUnPagoSinOrdenNiMetodo() {
        assertThrows(BusinessException.class, () -> Pago.nuevo(null, MetodoPago.PSE, BigDecimal.TEN));
        assertThrows(BusinessException.class, () -> Pago.nuevo(1L, null, BigDecimal.TEN));
    }

    @Test
    void aprobarDesdePendienteGuardaLaReferencia() {
        Pago pago = nuevoPago();

        pago.aprobar("REF-1");

        assertEquals(EstadoPago.APROBADO, pago.getEstado());
        assertEquals("REF-1", pago.getReferenciaPasarela());
    }

    @Test
    void rechazarDesdePendienteGuardaLaReferencia() {
        Pago pago = nuevoPago();

        pago.rechazar("REF-2");

        assertEquals(EstadoPago.RECHAZADO, pago.getEstado());
        assertEquals("REF-2", pago.getReferenciaPasarela());
    }

    @Test
    void unPagoYaResueltoNoSePuedeAprobarNiRechazarOtraVez() {
        Pago aprobado = nuevoPago();
        aprobado.aprobar("REF-1");
        Pago rechazado = nuevoPago();
        rechazado.rechazar("REF-2");

        assertThrows(BusinessException.class, () -> aprobado.aprobar("OTRA"));
        assertThrows(BusinessException.class, () -> aprobado.rechazar("OTRA"));
        assertThrows(BusinessException.class, () -> rechazado.aprobar("OTRA"));
    }

    @Test
    void soloUnPagoAprobadoSePuedeReembolsar() {
        Pago pendiente = nuevoPago();
        Pago rechazado = nuevoPago();
        rechazado.rechazar("REF");
        Pago aprobado = nuevoPago();
        aprobado.aprobar("REF");

        assertThrows(BusinessException.class, pendiente::reembolsar);
        assertThrows(BusinessException.class, rechazado::reembolsar);

        aprobado.reembolsar();
        assertEquals(EstadoPago.REEMBOLSADO, aprobado.getEstado());
    }

    @Test
    void unPagoNoSePuedeReembolsarDosVeces() {
        Pago pago = nuevoPago();
        pago.aprobar("REF");
        pago.reembolsar();

        assertThrows(BusinessException.class, pago::reembolsar);
    }

    @Test
    void metodoYEstadoSeConviertenDesdeTextoSinImportarMayusculas() {
        assertEquals(MetodoPago.PSE, MetodoPago.desde("pse"));
        assertEquals(MetodoPago.TARJETA, MetodoPago.desde("TARJETA"));
        assertEquals(EstadoPago.APROBADO, EstadoPago.desde("Aprobado"));
        assertThrows(BusinessException.class, () -> MetodoPago.desde("cheque"));
        assertThrows(BusinessException.class, () -> EstadoPago.desde("quiza"));
    }
}
