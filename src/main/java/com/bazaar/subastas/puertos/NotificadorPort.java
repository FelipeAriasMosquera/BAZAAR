package com.bazaar.subastas.puertos;

import java.math.BigDecimal;

public interface NotificadorPort {
    void pujaSuperada(Long usuarioAnteriorId, Long subastaId, BigDecimal nuevoMonto);
    void subastaGanada(Long ganadorId, Long subastaId, BigDecimal montoFinal);
}