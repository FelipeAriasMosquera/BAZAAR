package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.puertos.PasarelaPagoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrarPagoUseCaseTest {

    private PagosFakes.Ordenes ordenes;
    private PagosFakes.Pagos pagos;
    private PagosFakes.Pasarela pasarela;
    private RegistrarPagoUseCase useCase;

    @BeforeEach
    void preparar() {
        ordenes = new PagosFakes.Ordenes();
        pagos = new PagosFakes.Pagos();
        pasarela = new PagosFakes.Pasarela();
        useCase = new RegistrarPagoUseCase(pagos, pasarela, ordenes);
        ordenes.agregar(1001L, "150000.00", true);
    }

    @Test
    void siLaPasarelaApruebaElPagoQuedaAprobadoYLaOrdenSeMarcaComoPagada() {
        Pago pago = useCase.ejecutar(1001L, MetodoPago.TARJETA);

        assertEquals(EstadoPago.APROBADO, pago.getEstado());
        assertTrue(pago.getReferenciaPasarela().startsWith("REF-"));
        assertEquals(List.of(1001L), ordenes.marcadasComoPagadas);
        assertEquals(EstadoPago.APROBADO, pagos.buscarPorId(pago.getId()).orElseThrow().getEstado());
    }

    @Test
    void siLaPasarelaRechazaElPagoQuedaRechazadoYLaOrdenNoSeToca() {
        pasarela.aprobarCobros = false;

        Pago pago = useCase.ejecutar(1001L, MetodoPago.PSE);

        assertEquals(EstadoPago.RECHAZADO, pago.getEstado());
        assertTrue(ordenes.marcadasComoPagadas.isEmpty());
        assertTrue(ordenes.buscarPorId(1001L).orElseThrow().pendienteDePago());
    }

    @Test
    void elMontoSeTomaDeLaOrdenYSeLeEnviaAsiALaPasarela() {
        Pago pago = useCase.ejecutar(1001L, MetodoPago.WALLET);

        assertEquals(new BigDecimal("150000.00"), pago.getMonto());
        assertEquals(1, pasarela.cobros.size());
        assertEquals(new BigDecimal("150000.00"), pasarela.cobros.get(0).monto());
        assertEquals(MetodoPago.WALLET, pasarela.cobros.get(0).metodo());
        assertEquals(pago.getId(), pasarela.cobros.get(0).pagoId());
    }

    @Test
    void elPagoYaEstaGuardadoComoPendienteCuandoSeLlamaALaPasarela() {
        AtomicReference<EstadoPago> estadoVisto = new AtomicReference<>();
        PasarelaPagoPort espia = new PasarelaPagoPort() {
            @Override
            public ResultadoPasarela cobrar(SolicitudCobro solicitud) {
                estadoVisto.set(pagos.buscarPorId(solicitud.pagoId()).orElseThrow().getEstado());
                return ResultadoPasarela.aprobado("REF-ESPIA");
            }

            @Override
            public ResultadoPasarela reembolsar(String referenciaPago, BigDecimal monto) {
                return ResultadoPasarela.aprobado("RF-ESPIA");
            }
        };

        new RegistrarPagoUseCase(pagos, espia, ordenes).ejecutar(1001L, MetodoPago.TARJETA);

        assertEquals(EstadoPago.PENDIENTE, estadoVisto.get());
    }

    @Test
    void unaOrdenInexistenteLanzaNotFoundYNoCobraNada() {
        assertThrows(ResourceNotFoundException.class, () -> useCase.ejecutar(9999L, MetodoPago.TARJETA));

        assertTrue(pagos.almacen.isEmpty());
        assertTrue(pasarela.cobros.isEmpty());
    }

    @Test
    void unaOrdenQueNoEstaPendienteNoSePuedePagar() {
        ordenes.agregar(1003L, "230000.00", false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.ejecutar(1003L, MetodoPago.TARJETA));

        assertTrue(ex.getMessage().contains("no está pendiente"));
        assertTrue(pagos.almacen.isEmpty());
        assertTrue(pasarela.cobros.isEmpty());
    }

    @Test
    void unaOrdenSinDireccionDeEnvioNoSePuedePagar() {
        ordenes.agregar(1005L, "99000.00", true, false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.ejecutar(1005L, MetodoPago.TARJETA));

        assertTrue(ex.getMessage().contains("dirección de envío"));
        assertTrue(pagos.almacen.isEmpty());
        assertTrue(pasarela.cobros.isEmpty());
        assertTrue(ordenes.marcadasComoPagadas.isEmpty());
    }

    @Test
    void unaOrdenConUnPagoPendienteOAprobadoNoAdmiteOtroPago() {
        pagos.sembrar(1001L, "150000.00", EstadoPago.PENDIENTE, null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.ejecutar(1001L, MetodoPago.TARJETA));

        assertTrue(ex.getMessage().contains("ya tiene un pago"));
        assertTrue(pasarela.cobros.isEmpty());
    }

    @Test
    void despuesDeUnPagoRechazadoSePuedeReintentar() {
        pasarela.aprobarCobros = false;
        Pago primero = useCase.ejecutar(1001L, MetodoPago.TARJETA);
        assertEquals(EstadoPago.RECHAZADO, primero.getEstado());

        pasarela.aprobarCobros = true;
        Pago segundo = useCase.ejecutar(1001L, MetodoPago.TARJETA);

        assertEquals(EstadoPago.APROBADO, segundo.getEstado());
        assertEquals(2, pagos.buscarPorOrden(1001L).size());
        assertEquals(List.of(1001L), ordenes.marcadasComoPagadas);
    }
}
