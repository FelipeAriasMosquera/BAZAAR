package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.pagos.dominio.MetodoPago;
import com.bazaar.pagos.dominio.ResultadoPasarela;
import com.bazaar.pagos.dominio.SolicitudCobro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasarelaSimuladaAdapterTest {

    private final PasarelaSimuladaAdapter pasarela = new PasarelaSimuladaAdapter(new BigDecimal("5000000"));

    private SolicitudCobro cobroPor(String monto) {
        return new SolicitudCobro(1L, 1001L, new BigDecimal(monto), MetodoPago.TARJETA);
    }

    @Test
    void apruebaMontosHastaElMaximoIncluido() {
        assertTrue(pasarela.cobrar(cobroPor("150000.00")).exitoso());
        assertTrue(pasarela.cobrar(cobroPor("5000000.00")).exitoso());
    }

    @Test
    void rechazaMontosQueSuperanElMaximo() {
        ResultadoPasarela resultado = pasarela.cobrar(cobroPor("5000000.01"));

        assertFalse(resultado.exitoso());
        assertNotNull(resultado.mensaje());
    }

    @Test
    void siempreDevuelveUnaReferenciaConPrefijoSim() {
        assertTrue(pasarela.cobrar(cobroPor("100.00")).referencia().startsWith("SIM-"));
        assertTrue(pasarela.cobrar(cobroPor("9999999.00")).referencia().startsWith("SIM-"));
    }

    @Test
    void elReembolsoSiempreEsExitoso() {
        assertTrue(pasarela.reembolsar("SIM-123", new BigDecimal("150000.00")).exitoso());
    }
}
