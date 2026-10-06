package com.bazaar.catalogo.repository;

import com.bazaar.catalogo.entity.EstadoProducto;
import com.bazaar.catalogo.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Producto> findByEstado(EstadoProducto estado, Pageable pageable);

    Page<Producto> findByCategoria_IdAndEstado(Long categoriaId, EstadoProducto estado, Pageable pageable);

    Page<Producto> findByVendedorIdAndEstado(Long vendedorId, EstadoProducto estado, Pageable pageable);
}
