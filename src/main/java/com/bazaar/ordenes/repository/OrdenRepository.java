package com.bazaar.ordenes.repository;

import com.bazaar.ordenes.entity.EstadoOrden;
import com.bazaar.ordenes.entity.Orden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Page<Orden> findByUsuarioId(Long usuarioId, Pageable pageable);

    Optional<Orden> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Evita generar dos órdenes para la misma subasta si el job de cierre corre dos veces. */
    boolean existsByItems_SubastaId(Long subastaId);

    @Query("select count(o) > 0 from Orden o join o.items i "
            + "where o.usuario.id = :usuarioId and i.producto.id = :productoId and o.estado in :estados")
    boolean existeCompra(@Param("usuarioId") Long usuarioId,
                         @Param("productoId") Long productoId,
                         @Param("estados") Collection<EstadoOrden> estados);

}
