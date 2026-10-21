package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.dominio.OrdenParaPago;
import com.bazaar.pagos.puertos.OrdenPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TEMPORAL: órdenes ficticias mientras no exista el módulo "ordenes".
 * Cuando exista: crear OrdenAdapter (implements OrdenPort, llamando a ordenes.OrdenService)
 * y BORRAR esta clase; si quedan las dos, Spring falla al arrancar por tener dos OrdenPort.
 *
 * Vive en memoria: se reinicia con cada arranque de la app (devtools también reinicia).
 * Los ids y totales deben coincidir con sql/pagos_datos_dummy.sql, porque la tabla pagos
 * tiene una FK real a ordenes(id).
 *
 *   1001  150.000,00    pendiente   -> la pasarela simulada la aprueba
 *   1002   89.900,50    pendiente   -> la pasarela simulada la aprueba
 *   1003  230.000,00    ya pagada   -> sirve para probar el error "no está pendiente"
 *   1004  7.500.000,00  pendiente   -> la pasarela simulada la rechaza (supera el monto máximo)
 */
@Component
public class OrdenDummyAdapter implements OrdenPort {

    private static final Long USUARIO_DUMMY_ID = 1L;

    private final Map<Long, OrdenParaPago> ordenes = new ConcurrentHashMap<>();

    public OrdenDummyAdapter() {
        agregar(1001L, "150000.00", true);
        agregar(1002L, "89900.50", true);
        agregar(1003L, "230000.00", false);
        agregar(1004L, "7500000.00", true);
    }

    @Override
    public Optional<OrdenParaPago> buscarPorId(Long ordenId) {
        return Optional.ofNullable(ordenes.get(ordenId));
    }

    @Override
    public void marcarComoPagada(Long ordenId) {
        OrdenParaPago actual = ordenes.get(ordenId);
        if (actual == null) {
            throw new ResourceNotFoundException("Orden no encontrada con id " + ordenId);
        }
        ordenes.put(ordenId, new OrdenParaPago(actual.id(), actual.usuarioId(), actual.total(), false));
    }

    private void agregar(Long id, String total, boolean pendienteDePago) {
        ordenes.put(id, new OrdenParaPago(id, USUARIO_DUMMY_ID, new BigDecimal(total), pendienteDePago));
    }
}
