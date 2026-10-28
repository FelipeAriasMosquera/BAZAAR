package com.bazaar.pagos.dominio;

import com.bazaar.pagos.puertos.OrdenPort;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import com.bazaar.pagos.puertos.PasarelaPagoPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementaciones en memoria de los puertos, para probar los casos de uso sin Spring,
 * sin base de datos y sin Mockito. Esta es la ventaja práctica de la arquitectura hexagonal.
 */
final class PagosFakes {

    private PagosFakes() {
    }

    static class Pagos implements PagoRepositoryPort {
        final Map<Long, Pago> almacen = new LinkedHashMap<>();
        private long secuencia = 0;

        @Override
        public Pago guardar(Pago pago) {
            Long id = pago.getId() != null ? pago.getId() : ++secuencia;
            // Devuelve una copia, como hace el adaptador JPA real (entidad -> nuevo Pago de dominio).
            Pago guardado = Pago.reconstruir(id, pago.getOrdenId(), pago.getMetodo(), pago.getMonto(),
                    pago.getEstado(), pago.getReferenciaPasarela(), pago.getFecha());
            almacen.put(id, guardado);
            return guardado;
        }

        @Override
        public Optional<Pago> buscarPorId(Long id) {
            return Optional.ofNullable(almacen.get(id));
        }

        @Override
        public List<Pago> buscarPorOrden(Long ordenId) {
            return almacen.values().stream().filter(p -> p.getOrdenId().equals(ordenId)).toList();
        }

        @Override
        public boolean existePagoVigente(Long ordenId) {
            return almacen.values().stream().anyMatch(p -> p.getOrdenId().equals(ordenId)
                    && (p.getEstado() == EstadoPago.PENDIENTE || p.getEstado() == EstadoPago.APROBADO));
        }

        /** Atajo para dejar un pago ya guardado en el estado que necesite el test. */
        Pago sembrar(Long ordenId, String monto, EstadoPago estado, String referencia) {
            return guardar(Pago.reconstruir(null, ordenId, MetodoPago.TARJETA, new BigDecimal(monto),
                    estado, referencia, java.time.Instant.now()));
        }
    }

    static class Ordenes implements OrdenPort {
        final Map<Long, OrdenParaPago> almacen = new LinkedHashMap<>();
        final List<Long> marcadasComoPagadas = new ArrayList<>();

        void agregar(Long id, String total, boolean pendienteDePago) {
            agregar(id, total, pendienteDePago, true);
        }

        void agregar(Long id, String total, boolean pendienteDePago, boolean tieneDireccionDeEnvio) {
            almacen.put(id, new OrdenParaPago(id, 1L, new BigDecimal(total), pendienteDePago, tieneDireccionDeEnvio));
        }

        @Override
        public Optional<OrdenParaPago> buscarPorId(Long ordenId) {
            return Optional.ofNullable(almacen.get(ordenId));
        }

        @Override
        public void marcarComoPagada(Long ordenId) {
            marcadasComoPagadas.add(ordenId);
            OrdenParaPago o = almacen.get(ordenId);
            almacen.put(ordenId, new OrdenParaPago(o.id(), o.usuarioId(), o.total(), false, o.tieneDireccionDeEnvio()));
        }
    }

    static class Pasarela implements PasarelaPagoPort {
        boolean aprobarCobros = true;
        boolean aprobarReembolsos = true;
        final List<SolicitudCobro> cobros = new ArrayList<>();
        final List<String> referenciasReembolsadas = new ArrayList<>();
        final List<BigDecimal> montosReembolsados = new ArrayList<>();

        @Override
        public ResultadoPasarela cobrar(SolicitudCobro solicitud) {
            cobros.add(solicitud);
            return aprobarCobros
                    ? ResultadoPasarela.aprobado("REF-" + solicitud.pagoId())
                    : ResultadoPasarela.rechazado("REF-" + solicitud.pagoId(), "Fondos insuficientes");
        }

        @Override
        public ResultadoPasarela reembolsar(String referenciaPago, BigDecimal monto) {
            referenciasReembolsadas.add(referenciaPago);
            montosReembolsados.add(monto);
            return aprobarReembolsos
                    ? ResultadoPasarela.aprobado("RF-" + referenciaPago)
                    : ResultadoPasarela.rechazado(null, "Reembolso no permitido");
        }
    }
}
