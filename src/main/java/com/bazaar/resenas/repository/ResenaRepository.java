package com.bazaar.resenas.repository;

import com.bazaar.resenas.entity.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    Page<Resena> findByProducto_Id(Long productoId, Pageable pageable);

    Page<Resena> findByUsuario_Id(Long usuarioId, Pageable pageable);

    boolean existsByProducto_IdAndUsuario_Id(Long productoId, Long usuarioId);

    @Query("select avg(r.calificacion) as promedio, count(r) as total "
            + "from Resena r where r.producto.id = :productoId")
    ResumenProjection resumenPorProducto(@Param("productoId") Long productoId);

    interface ResumenProjection {
        Double getPromedio();

        Long getTotal();
    }
}
