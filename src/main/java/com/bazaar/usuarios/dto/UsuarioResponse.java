package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.model.Rol;

import java.util.Set;

public record UsuarioResponse(Long id, String nombre, String email, String telefono, Set<Rol> roles) {
}
