package com.bazaar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ActualizarPerfilRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 80) String apellido,
        @Size(max = 30) String telefono,
        @Past LocalDate fechaNacimiento,
        @Size(max = 2) String pais,
        @Size(max = 5) String idioma
) {}