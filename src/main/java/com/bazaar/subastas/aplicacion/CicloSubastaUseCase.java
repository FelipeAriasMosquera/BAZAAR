package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.Subasta;
import com.bazaar.subastas.puertos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicloSubastaUseCase {

    private final SubastaRepositoryPort subastas;
    private final NotificadorPort notificador;
    private final OrdenPort ordenes;
    private final TransactionTemplate tx;

    public void activarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Subasta s : subastas.buscarPorActivarAntesDe(ahora)) {
            try {
                tx.executeWithoutResult(st ->
                        subastas.buscarParaActualizar(s.getId()).ifPresent(b -> {
                            if (b.activar(ahora)) subastas.guardar(b);
                        }));
            } catch (RuntimeException e) {
                log.error("No se pudo activar la subasta {}", s.getId(), e);
            }
        }
    }

    public void cerrarVencidas() {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Subasta s : subastas.buscarPorCerrarAntesDe(ahora)) {
            try {
                tx.executeWithoutResult(st ->
                        subastas.buscarParaActualizar(s.getId()).ifPresent(b -> {
                            boolean hayGanador = b.cerrar(ahora);
                            subastas.guardar(b);
                            if (hayGanador) {
                                ordenes.crearOrdenPorSubastaGanada(
                                        b.getGanadorId(), b.getId(), b.getProductoId(), b.getPrecioActual());
                                notificador.subastaGanada(b.getGanadorId(), b.getId(), b.getPrecioActual());
                            }
                        }));
            } catch (RuntimeException e) {
                log.error("No se pudo cerrar la subasta {}", s.getId(), e);
            }
        }
    }
}