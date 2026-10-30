package com.bazaar.pagos.dominio;

import java.math.BigDecimal;

/**
 * La orden tal como la necesita el módulo de pagos: solo lo que hace falta para
 * cobrarla. Pagos no conoce items ni envío; eso es de "ordenes".
 *
 * @param pendienteDePago       la decide quien implementa OrdenPort según el estado real de la
 *                              orden; pagos no interpreta los estados de ordenes.
 * @param tieneDireccionDeEnvio una orden ya pagada no admite cambiar su dirección (solo las
 *                              pendientes), y no se puede enviar sin dirección. Por eso pagos no
 *                              cobra una orden sin dirección: quedaría pagada y atascada.
 */
public record OrdenParaPago(Long id, Long usuarioId, BigDecimal total, String emailPropietario,
                            boolean pendienteDePago, boolean tieneDireccionDeEnvio) {

    public boolean perteneceA(String email) {
        return emailPropietario != null && emailPropietario.equalsIgnoreCase(email);
    }
}
