package com.bazaar.catalogo.dto;

public record CategoriaResponse(
        Long id,
        String nombre,
        String slug,
        Long categoriaPadreId,
        String categoriaPadreNombre
) {}
