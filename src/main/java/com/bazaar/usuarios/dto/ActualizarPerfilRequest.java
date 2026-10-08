package com.bazaar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarPerfilRequest(
        @NotBlank String nombre,
        String telefono
) {}