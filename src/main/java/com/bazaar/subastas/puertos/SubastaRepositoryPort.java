package com.bazaar.subastas.puertos;

import com.bazaar.subastas.dominio.EstadoSubasta;
import com.bazaar.subastas.dominio.Puja;
import com.bazaar.subastas.dominio.Subasta;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SubastaRepositoryPort {
    Optional<Subasta> buscarParaActualizar(Long id);   // SELECT ... FOR UPDATE
    Optional<Subasta> buscarPorId(Long id);
    List<Subasta> listarPorEstado(EstadoSubasta estado);
    List<Subasta> buscarPorActivarAntesDe(OffsetDateTime ahora);
    List<Subasta> buscarPorCerrarAntesDe(OffsetDateTime ahora);
    Subasta guardar(Subasta subasta);
    Puja guardarPuja(Puja puja);
    List<Puja> listarPujas(Long subastaId);
    boolean existePorProducto(Long productoId);

}