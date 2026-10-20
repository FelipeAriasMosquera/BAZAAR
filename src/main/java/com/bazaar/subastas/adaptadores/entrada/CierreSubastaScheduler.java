package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.subastas.aplicacion.CicloSubastaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CierreSubastaScheduler {

    private final CicloSubastaUseCase ciclo;

    @Scheduled(fixedDelay = 30_000)
    public void tick() {
        ciclo.activarPendientes();
        ciclo.cerrarVencidas();
    }
}