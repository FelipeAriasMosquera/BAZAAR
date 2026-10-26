package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.ordenes.service.OrdenService;
import com.bazaar.subastas.puertos.OrdenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrdenAdapter implements OrdenPort {

    private final OrdenService ordenService;

    @Override
    public void crearOrdenPorSubastaGanada(Long ganadorId, Long subastaId, Long productoId, BigDecimal montoFinal) {
        // OJO con el orden de parámetros: su método recibe (usuarioId, productoId, subastaId, monto)
        ordenService.crearDesdeSubasta(ganadorId, productoId, subastaId, montoFinal);
    }
}