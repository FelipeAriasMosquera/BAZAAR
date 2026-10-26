package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.puertos.UsuarioPort;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarios;

    @Override
    public Long obtenerIdPorEmail(String email) {
        return usuarios.findByEmail(email).orElseThrow().getId();
    }

    @Override
    public Map<Long, String> nombresPorId(Collection<Long> ids) {
        // una sola consulta para todos los usuarios, sin una consulta por puja
        return usuarios.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre));
    }
}