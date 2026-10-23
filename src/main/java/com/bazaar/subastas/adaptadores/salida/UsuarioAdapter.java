package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.puertos.UsuarioPort;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarios;

    @Override
    public Long obtenerIdPorEmail(String email) {
        return usuarios.findByEmail(email).orElseThrow().getId();
    }
}