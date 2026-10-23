package com.bazaar.pagos.adaptadores.entrada;

import com.bazaar.pagos.adaptadores.entrada.dto.PagoResponse;
import com.bazaar.pagos.dominio.Pago;

public final class PagoMapper {

    private PagoMapper() {
    }

    public static PagoResponse toResponse(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getOrdenId(),
                pago.getMetodo().getValor(),
                pago.getMonto(),
                pago.getEstado().getValor(),
                pago.getReferenciaPasarela(),
                pago.getFecha());
    }
}
