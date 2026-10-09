package com.bazaar.catalogo.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductoImagenRequest(
        @NotBlank(message = "La url de la imagen es obligatoria")
        String url,
        Short orden
) {}
