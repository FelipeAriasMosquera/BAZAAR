package com.bazaar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String passwordNueva
) {}