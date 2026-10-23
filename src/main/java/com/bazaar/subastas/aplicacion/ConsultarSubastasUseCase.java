package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.*;
import com.bazaar.subastas.puertos.SubastaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarSubastasUseCase {

    private final SubastaRepositoryPort subastas;

    public List<Subasta> listarActivas() {
        return subastas.listarPorEstado(EstadoSubasta.ACTIVA);
    }

    public Subasta obtener(Long id) {
        return subastas.buscarPorId(id).orElseThrow(() -> new SubastaNoEncontradaException(id));
    }

    public List<Puja> historial(Long subastaId) {
        obtener(subastaId);   // lanza 404 si no existe
        return subastas.listarPujas(subastaId);
    }
}