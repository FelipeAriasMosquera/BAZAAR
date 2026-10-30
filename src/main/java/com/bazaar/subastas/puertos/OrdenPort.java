package com.bazaar.subastas.puertos;

import java.math.BigDecimal;

public interface OrdenPort {
    void crearOrdenPorSubastaGanada(Long ganadorId, Long subastaId, Long productoId, BigDecimal montoFinal);
}