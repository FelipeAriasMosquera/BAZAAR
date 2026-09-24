package com.bazaar.catalogo.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<Categoria> findBySlug(String slug);

    List<Categoria> findByCategoriaPadreIsNull();

    List<Categoria> findByCategoriaPadre_Id(Long categoriaPadreId);

    long countByCategoriaPadre_Id(Long categoriaPadreId);
}
