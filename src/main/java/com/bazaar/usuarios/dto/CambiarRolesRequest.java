package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.model.Rol;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CambiarRolesRequest(@NotEmpty Set<Rol> roles) {
}
