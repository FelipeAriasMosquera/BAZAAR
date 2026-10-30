package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.*;
import com.bazaar.subastas.puertos.SubastaRepositoryPort;
import com.bazaar.subastas.puertos.UsuarioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultarSubastasUseCase {

    private final SubastaRepositoryPort subastas;
    private final UsuarioPort usuarios;

    public List<Subasta> listarActivas() {
        return subastas.listarPorEstado(EstadoSubasta.ACTIVA);
    }

    public Subasta obtener(Long id) {
        return subastas.buscarPorId(id).orElseThrow(() -> new SubastaNoEncontradaException(id));
    }

    public List<PujaDetalle> historialDetallado(Long subastaId) {
        obtener(subastaId);   // 404 si no existe
        List<Puja> pujas = subastas.listarPujas(subastaId);
        Map<Long, String> nombres = usuarios.nombresPorId(
                pujas.stream().map(Puja::usuarioId).collect(Collectors.toSet()));
        return pujas.stream()
                .map(p -> new PujaDetalle(p.id(), p.usuarioId(),
                        nombres.getOrDefault(p.usuarioId(), "Usuario"), p.monto(), p.fecha()))
                .toList();
    }
}