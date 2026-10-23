package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import com.bazaar.pagos.puertos.PasarelaPagoPort;

/**
 * Reembolsa un pago APROBADO. Primero valida el estado y solo después llama a la pasarela:
 * así nunca se devuelve dinero de un pago que no se puede reembolsar.
 *
 * Decisión de negocio pendiente: qué le pasa a la orden al reembolsar (¿vuelve a
 * 'pendiente' o pasa a 'cancelada'?). Hoy solo cambia el pago; cuando exista el módulo
 * ordenes se añade el método correspondiente a OrdenPort.
 */
public class ReembolsarPagoUseCase {

    private final PagoRepositoryPort pagos;
    private final PasarelaPagoPort pasarela;

    public ReembolsarPagoUseCase(PagoRepositoryPort pagos, PasarelaPagoPort pasarela) {
        this.pagos = pagos;
        this.pasarela = pasarela;
    }

    public Pago ejecutar(Long pagoId) {
        Pago pago = pagos.buscarPorId(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id " + pagoId));

        pago.verificarReembolsable();

        ResultadoPasarela resultado = pasarela.reembolsar(pago.getReferenciaPasarela(), pago.getMonto());
        if (!resultado.exitoso()) {
            throw new BusinessException("La pasarela rechazó el reembolso: " + resultado.mensaje());
        }

        pago.reembolsar();
        return pagos.guardar(pago);
    }
}
