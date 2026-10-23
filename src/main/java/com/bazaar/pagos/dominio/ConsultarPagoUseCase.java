package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.puertos.PagoRepositoryPort;

import java.util.List;

/** Consultas de pagos. Solo lectura. */
public class ConsultarPagoUseCase {

    private final PagoRepositoryPort pagos;

    public ConsultarPagoUseCase(PagoRepositoryPort pagos) {
        this.pagos = pagos;
    }

    public Pago porId(Long id) {
        return pagos.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id " + id));
    }

    public List<Pago> porOrden(Long ordenId) {
        return pagos.buscarPorOrden(ordenId);
    }
}
