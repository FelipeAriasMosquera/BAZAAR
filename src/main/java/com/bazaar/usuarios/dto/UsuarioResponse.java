package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.model.Rol;

public record UsuarioResponse(Long id, String nombre, String email, String telefono, Rol rol) {
}
