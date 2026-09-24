package com.bazaar.catalogo.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Si "slug" viene vacío, el servicio lo genera automáticamente a partir de "nombre".
 */
public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 120, message = "El slug no puede superar 120 caracteres")
        String slug,

        Long categoriaPadreId
) {}
