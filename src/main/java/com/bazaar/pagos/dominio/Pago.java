package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Entidad de dominio. No tiene anotaciones de JPA ni de Spring: la tabla "pagos"
 * se mapea aparte, en adaptadores.salida.PagoEntity.
 *
 * Las transiciones de estado viven aquí (no en el controller ni en un servicio)
 * para que sea imposible, por ejemplo, reembolsar un pago que nunca fue aprobado:
 *
 *   PENDIENTE ──► APROBADO ──► REEMBOLSADO
 *       └───────► RECHAZADO
 */
public class Pago {

    private final Long id;
    private final Long ordenId;
    private final MetodoPago metodo;
    private final BigDecimal monto;
    private final Instant fecha;
    private EstadoPago estado;
    private String referenciaPasarela;

    private Pago(Long id, Long ordenId, MetodoPago metodo, BigDecimal monto,
                 EstadoPago estado, String referenciaPasarela, Instant fecha) {
        this.id = id;
        this.ordenId = ordenId;
        this.metodo = metodo;
        this.monto = monto;
        this.estado = estado;
        this.referenciaPasarela = referenciaPasarela;
        this.fecha = fecha;
    }

    /** Un pago recién creado siempre nace PENDIENTE y sin id (lo asigna la persistencia). */
    public static Pago nuevo(Long ordenId, MetodoPago metodo, BigDecimal monto) {
        if (ordenId == null) {
            throw new BusinessException("El pago debe pertenecer a una orden");
        }
        if (metodo == null) {
            throw new BusinessException("El método de pago es obligatorio");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new BusinessException("El monto del pago debe ser mayor a cero");
        }
        return new Pago(null, ordenId, metodo, monto.setScale(2, RoundingMode.HALF_UP),
                EstadoPago.PENDIENTE, null, Instant.now());
    }

    /** Para que el adaptador de persistencia reconstruya un pago ya guardado. No valida nada. */
    public static Pago reconstruir(Long id, Long ordenId, MetodoPago metodo, BigDecimal monto,
                                   EstadoPago estado, String referenciaPasarela, Instant fecha) {
        return new Pago(id, ordenId, metodo, monto, estado, referenciaPasarela, fecha);
    }

    public void aprobar(String referencia) {
        exigirEstado(EstadoPago.PENDIENTE, "aprobar");
        this.estado = EstadoPago.APROBADO;
        this.referenciaPasarela = referencia;
    }

    public void rechazar(String referencia) {
        exigirEstado(EstadoPago.PENDIENTE, "rechazar");
        this.estado = EstadoPago.RECHAZADO;
        this.referenciaPasarela = referencia;
    }

    /** Se separa de reembolsar() para poder validar ANTES de llamar a la pasarela. */
    public void verificarReembolsable() {
        exigirEstado(EstadoPago.APROBADO, "reembolsar");
    }

    public void reembolsar() {
        verificarReembolsable();
        this.estado = EstadoPago.REEMBOLSADO;
    }

    private void exigirEstado(EstadoPago requerido, String accion) {
        if (estado != requerido) {
            throw new BusinessException(
                    "No se puede " + accion + " un pago en estado '" + estado.getValor() + "'");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getReferenciaPasarela() {
        return referenciaPasarela;
    }

    public Instant getFecha() {
        return fecha;
    }
}
