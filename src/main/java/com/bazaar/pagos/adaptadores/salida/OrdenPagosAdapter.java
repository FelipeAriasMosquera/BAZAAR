package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.ordenes.entity.EstadoOrden;
import com.bazaar.ordenes.repository.OrdenRepository;
import com.bazaar.ordenes.service.OrdenService;
import com.bazaar.pagos.dominio.OrdenParaPago;
import com.bazaar.pagos.puertos.OrdenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Conecta pagos con el módulo real de órdenes.
 *
 * El nombre es OrdenPagosAdapter (y no OrdenAdapter) a propósito: subastas ya tiene su propio
 * OrdenAdapter, y dos clases @Component con el mismo nombre hacen que Spring falle al arrancar.
 *
 * - Leer: usa OrdenRepository. Los métodos de lectura de OrdenService (obtener, listarPorUsuario)
 *   están atados al usuario que tiene la sesión abierta, y pagos necesita poder buscar la orden
 *   por su id sin eso (por ejemplo cuando la pasarela confirme un pago más adelante).
 * - Escribir: usa OrdenService.marcarComoPagada, que ya valida la transición de estado
 *   (pendiente -> pagada) y es el punto de conexión que ordenes previó para pagos.
 *
 * ordenes NO debe importar nada de pagos (dependencia circular).
 *
 * Mejora futura: que OrdenService ofrezca un método público de lectura para pagos; entonces
 * este adaptador deja de depender del repositorio de otro módulo.
 */
@Component
@RequiredArgsConstructor
public class OrdenPagosAdapter implements OrdenPort {

    private final OrdenRepository ordenRepository;
    private final OrdenService ordenService;

    /** @Transactional porque orden.getUsuario() es una carga perezosa y los casos de uso de pagos no abren transacción. */
    @Override
    @Transactional(readOnly = true)
    public Optional<OrdenParaPago> buscarPorId(Long ordenId) {
        return ordenRepository.findById(ordenId).map(orden -> new OrdenParaPago(
                orden.getId(),
                orden.getUsuario().getId(),
                orden.getTotal(),
                // Se usa la regla de la propia orden: así, si cambia su ciclo de vida, pagos lo respeta.
                orden.getEstado().permiteTransicionA(EstadoOrden.PAGADA),
                orden.getDireccionId() != null));
    }

    @Override
    public void marcarComoPagada(Long ordenId) {
        ordenService.marcarComoPagada(ordenId);
    }
}
