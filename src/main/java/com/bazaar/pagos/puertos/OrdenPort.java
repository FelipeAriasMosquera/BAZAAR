package com.bazaar.pagos.puertos;

import com.bazaar.pagos.dominio.OrdenParaPago;

import java.util.Optional;

/**
 * Puerto de salida hacia el módulo "ordenes". Lo implementa OrdenPagosAdapter, que usa el módulo
 * real de órdenes. Si ordenes cambia, solo se toca ese adaptador; el dominio de pagos no cambia.
 */
public interface OrdenPort {

    Optional<OrdenParaPago> buscarPorId(Long ordenId);

    void marcarComoPagada(Long ordenId);
}
