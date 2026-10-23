package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.puertos.NotificadorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Slf4j
@Component
public class NotificadorLogAdapter implements NotificadorPort {

    @Override
    public void pujaSuperada(Long usuarioId, Long subastaId, BigDecimal monto) {
        log.info("[NOTIF FAKE] Usuario {} fue superado en la subasta {} con {}", usuarioId, subastaId, monto);
    }

    @Override
    public void subastaGanada(Long ganadorId, Long subastaId, BigDecimal monto) {
        log.info("[NOTIF FAKE] Usuario {} ganó la subasta {} por {}", ganadorId, subastaId, monto);
    }
}