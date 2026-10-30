package com.bazaar.notificaciones.repository;

import com.bazaar.notificaciones.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    Page<Notificacion> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndLeidoFalse(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndLeidoFalse(Long usuarioId);

    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("update Notificacion n set n.leido = true where n.usuario.id = :usuarioId and n.leido = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
}
