package com.bazaar.pagos.dominio;

import java.math.BigDecimal;

/**
 * La orden tal como la necesita el módulo de pagos: solo lo que hace falta para
 * cobrarla. Pagos no conoce items, direcciones ni envío; eso es de "ordenes".
 *
 * @param pendienteDePago la decide quien implementa OrdenPort según el estado real de
 *                        la orden (en la tabla: 'pendiente'); pagos no interpreta esos estados.
 */
public record OrdenParaPago(Long id, Long usuarioId, BigDecimal total, boolean pendienteDePago) {
}
