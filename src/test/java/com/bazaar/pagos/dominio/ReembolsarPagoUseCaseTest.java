package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReembolsarPagoUseCaseTest {

    private PagosFakes.Pagos pagos;
    private PagosFakes.Pasarela pasarela;
    private ReembolsarPagoUseCase useCase;

    @BeforeEach
    void preparar() {
        pagos = new PagosFakes.Pagos();
        pasarela = new PagosFakes.Pasarela();
        useCase = new ReembolsarPagoUseCase(pagos, pasarela);
    }

    @Test
    void reembolsaUnPagoAprobadoYLeEnviaLaReferenciaYElMontoALaPasarela() {
        Pago aprobado = pagos.sembrar(1001L, "150000.00", EstadoPago.APROBADO, "REF-1");

        Pago resultado = useCase.ejecutar(aprobado.getId());

        assertEquals(EstadoPago.REEMBOLSADO, resultado.getEstado());
        assertEquals(EstadoPago.REEMBOLSADO, pagos.buscarPorId(aprobado.getId()).orElseThrow().getEstado());
        assertEquals(List.of("REF-1"), pasarela.referenciasReembolsadas);
        assertEquals(new BigDecimal("150000.00"), pasarela.montosReembolsados.get(0));
    }

    @Test
    void unPagoQueNoEstaAprobadoNoSeReembolsaNiSeLlamaALaPasarela() {
        Pago pendiente = pagos.sembrar(1001L, "150000.00", EstadoPago.PENDIENTE, null);
        Pago rechazado = pagos.sembrar(1002L, "89900.50", EstadoPago.RECHAZADO, "REF-2");

        assertThrows(BusinessException.class, () -> useCase.ejecutar(pendiente.getId()));
        assertThrows(BusinessException.class, () -> useCase.ejecutar(rechazado.getId()));
        assertTrue(pasarela.referenciasReembolsadas.isEmpty());
    }

    @Test
    void siLaPasarelaRechazaElReembolsoElPagoSigueAprobado() {
        Pago aprobado = pagos.sembrar(1001L, "150000.00", EstadoPago.APROBADO, "REF-1");
        pasarela.aprobarReembolsos = false;

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.ejecutar(aprobado.getId()));

        assertTrue(ex.getMessage().contains("Reembolso no permitido"));
        assertEquals(EstadoPago.APROBADO, pagos.buscarPorId(aprobado.getId()).orElseThrow().getEstado());
    }

    @Test
    void unPagoInexistenteLanzaNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> useCase.ejecutar(9999L));
    }

    @Test
    void unPagoYaReembolsadoNoSePuedeReembolsarOtraVez() {
        Pago aprobado = pagos.sembrar(1001L, "150000.00", EstadoPago.APROBADO, "REF-1");
        useCase.ejecutar(aprobado.getId());

        assertThrows(BusinessException.class, () -> useCase.ejecutar(aprobado.getId()));
        assertEquals(1, pasarela.referenciasReembolsadas.size());
    }
}

