package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.Subasta;
import com.bazaar.subastas.puertos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CicloSubastaUseCase {

    private final SubastaRepositoryPort subastas;
    private final NotificadorPort notificador;
    private final OrdenPort ordenes;
    private final TransactionTemplate tx;   // una transacción por subasta: si una falla, las demás siguen

    public void activarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Subasta s : subastas.buscarPorActivarAntesDe(ahora)) {
            tx.executeWithoutResult(status ->
                    subastas.buscarParaActualizar(s.getId()).ifPresent(b -> {
                        if (b.activar(ahora)) subastas.guardar(b);
                    }));
        }
    }

    public void cerrarVencidas() {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Subasta s : subastas.buscarPorCerrarAntesDe(ahora)) {
            tx.executeWithoutResult(status ->
                    subastas.buscarParaActualizar(s.getId()).ifPresent(b -> {
                        boolean hayGanador = b.cerrar(ahora);
                        subastas.guardar(b);
                        if (hayGanador) {
                            ordenes.crearOrdenPorSubastaGanada(b.getGanadorId(), b.getId(), b.getPrecioActual());
                            notificador.subastaGanada(b.getGanadorId(), b.getId(), b.getPrecioActual());
                        }
                    }));
        }
    }
}