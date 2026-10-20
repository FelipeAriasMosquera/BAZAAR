package com.bazaar.subastas.adaptadores.salida;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SubastaJpaRepository extends JpaRepository<SubastaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)   // SELECT ... FOR UPDATE (RNF-01)
    @Query("select s from SubastaEntity s where s.id = :id")
    Optional<SubastaEntity> findByIdForUpdate(@Param("id") Long id);

    List<SubastaEntity> findByEstado(String estado);
    List<SubastaEntity> findByEstadoAndFechaInicioLessThanEqual(String estado, OffsetDateTime ahora);
    List<SubastaEntity> findByEstadoAndFechaFinLessThanEqual(String estado, OffsetDateTime ahora);

    // Solo leemos la columna vendedor_id para no acoplarnos a la entidad del catálogo
    @Query(value = "select vendedor_id from productos where id = :productoId", nativeQuery = true)
    Long findVendedorIdByProductoId(@Param("productoId") Long productoId);
}