package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.*;
import com.bazaar.subastas.puertos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RegistrarPujaUseCase {

    private final SubastaRepositoryPort subastas;
    private final NotificadorPort notificador;

    @Transactional   // el bloqueo FOR UPDATE dura hasta el commit: serializa las pujas concurrentes
    public Puja ejecutar(Long subastaId, Long usuarioId, BigDecimal monto) {
        Subasta subasta = subastas.buscarParaActualizar(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));

        Long lideranteAnterior = subasta.getGanadorId();
        Puja puja = subasta.registrarPuja(usuarioId, monto, OffsetDateTime.now());

        subastas.guardar(subasta);
        Puja guardada = subastas.guardarPuja(puja);

        if (lideranteAnterior != null && !lideranteAnterior.equals(usuarioId)) {
            notificador.pujaSuperada(lideranteAnterior, subastaId, monto);
        }
        return guardada;
    }
}