package com.bazaar.catalogo.categoria.mapper;

import com.bazaar.catalogo.categoria.dto.CategoriaResponse;
import com.bazaar.catalogo.categoria.entity.Categoria;

public final class CategoriaMapper {

    private CategoriaMapper() {}

    public static CategoriaResponse toResponse(Categoria categoria) {
        Categoria padre = categoria.getCategoriaPadre();
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getSlug(),
                padre != null ? padre.getId() : null,
                padre != null ? padre.getNombre() : null
        );
    }
}
