package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.puertos.OrdenPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Slf4j
@Component
public class OrdenLogAdapter implements OrdenPort {

    @Override
    public void crearOrdenPorSubastaGanada(Long ganadorId, Long subastaId, BigDecimal monto) {
        log.info("[ORDEN FAKE] Orden pendiente de pago: usuario {} subasta {} monto {}", ganadorId, subastaId, monto);
    }
}