package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.entity.Usuario;
import lombok.Getter;

@Getter
public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String email;
    private final String telefono;
    private final String rol;

    public UsuarioResponse(Usuario u) {
        this.id = u.getId();
        this.nombre = u.getNombre();
        this.email = u.getEmail();
        this.telefono = u.getTelefono();
        this.rol = u.getRol().name();
    }
}
