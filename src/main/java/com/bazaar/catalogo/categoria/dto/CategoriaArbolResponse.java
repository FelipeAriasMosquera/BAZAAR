package com.bazaar.catalogo.categoria.dto;

import java.util.List;

public record CategoriaArbolResponse(
        Long id,
        String nombre,
        String slug,
        List<CategoriaArbolResponse> subcategorias
) {}
