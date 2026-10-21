package com.bazaar.ordenes.repository;

import com.bazaar.ordenes.entity.Orden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Page<Orden> findByUsuarioId(Long usuarioId, Pageable pageable);

    Optional<Orden> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Evita generar dos órdenes para la misma subasta si el job de cierre corre dos veces. */
    boolean existsByItems_SubastaId(Long subastaId);
}
