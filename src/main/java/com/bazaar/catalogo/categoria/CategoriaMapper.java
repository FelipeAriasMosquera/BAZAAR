package com.bazaar.catalogo.categoria;

import com.bazaar.catalogo.categoria.dto.CategoriaResponse;

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
