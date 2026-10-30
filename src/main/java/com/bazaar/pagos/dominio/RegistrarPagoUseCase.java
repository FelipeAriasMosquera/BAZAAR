package com.bazaar.pagos.dominio;


import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.puertos.OrdenPort;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import com.bazaar.pagos.puertos.PasarelaPagoPort;

/**
 * Cobra una orden. Sin anotaciones de Spring: se registra como bean en PagosConfig.
 *
 * Decisiones que conviene no cambiar sin pensarlo:
 *  - El monto sale de la ORDEN, nunca del request: el cliente no decide cuánto paga.
 *  - El pago se guarda PENDIENTE *antes* de llamar a la pasarela. Si algo falla en
 *    medio queda rastro en BD para conciliar, en vez de un cobro sin registro.
 *  - No hay una transacción envolviendo todo: no se debe mantener una conexión abierta
 *    mientras se espera a un servicio externo. Cada guardado es atómico por sí solo.
 *  - Un rechazo de la pasarela NO es una excepción: es un pago RECHAZADO válido, y la
 *    orden sigue pendiente para poder reintentar.
 *
 * Pendiente cuando se conecte una pasarela real: qué hacer si cobrar() lanza una excepción
 * o expira (el pago se queda PENDIENTE y bloquea nuevos intentos hasta conciliarlo).
 */
public class RegistrarPagoUseCase {

    private final PagoRepositoryPort pagos;
    private final PasarelaPagoPort pasarela;
    private final OrdenPort ordenes;

    public RegistrarPagoUseCase(PagoRepositoryPort pagos, PasarelaPagoPort pasarela, OrdenPort ordenes) {
        this.pagos = pagos;
        this.pasarela = pasarela;
        this.ordenes = ordenes;
    }

    public Pago ejecutar(Long ordenId, MetodoPago metodo, String emailSolicitante) {
        OrdenParaPago orden = ordenes.buscarPorId(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con id " + ordenId));

        if (!orden.perteneceA(emailSolicitante)) {
            throw new ResourceNotFoundException("Orden no encontrada con id " + ordenId);
        }
        if (!orden.pendienteDePago()) {
            throw new BusinessException("La orden " + ordenId + " no está pendiente de pago");
        }
        if (!orden.tieneDireccionDeEnvio()) {
            throw new BusinessException("La orden " + ordenId + " no tiene dirección de envío: asígnala antes de pagar");
        }
        if (pagos.existePagoVigente(ordenId)) {
            throw new BusinessException("La orden " + ordenId + " ya tiene un pago pendiente o aprobado");
        }


        Pago pago = pagos.guardar(Pago.nuevo(ordenId, metodo, orden.total()));

        ResultadoPasarela resultado = pasarela.cobrar(
                new SolicitudCobro(pago.getId(), ordenId, pago.getMonto(), metodo));

        if (resultado.exitoso()) {
            pago.aprobar(resultado.referencia());
        } else {
            pago.rechazar(resultado.referencia());
        }
        pago = pagos.guardar(pago);

        if (pago.getEstado() == EstadoPago.APROBADO) {
            ordenes.marcarComoPagada(ordenId);
        }
        return pago;
    }
}
