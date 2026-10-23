package com.bazaar.pagos.puertos;

import com.bazaar.pagos.dominio.OrdenParaPago;

import java.util.Optional;

/**
 * Puerto de salida hacia el módulo "ordenes". Mientras ese módulo no exista lo implementa
 * OrdenDummyAdapter (datos ficticios). Cuando exista, se crea un OrdenAdapter que llame a
 * ordenes.OrdenService, se borra el dummy y el dominio de pagos no cambia.
 */
public interface OrdenPort {

    Optional<OrdenParaPago> buscarPorId(Long ordenId);

    void marcarComoPagada(Long ordenId);
}
