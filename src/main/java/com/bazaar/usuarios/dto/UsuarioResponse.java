package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.model.Rol;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        String pais,
        String idioma,
        Set<Rol> roles,
        OffsetDateTime fechaRegistro
) {}