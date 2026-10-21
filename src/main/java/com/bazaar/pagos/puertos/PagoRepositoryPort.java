package com.bazaar.pagos.puertos;

import com.bazaar.pagos.dominio.Pago;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: cómo el dominio guarda y busca pagos, sin saber si es JPA, memoria u otra cosa. */
public interface PagoRepositoryPort {

    /** Inserta si el pago no tiene id y actualiza si ya lo tiene. Devuelve el pago ya persistido (con id). */
    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(Long id);

    /** Del más reciente al más antiguo. */
    List<Pago> buscarPorOrden(Long ordenId);

    /** true si la orden tiene un pago PENDIENTE o APROBADO (los que impiden cobrarla otra vez). */
    boolean existePagoVigente(Long ordenId);
}
