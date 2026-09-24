package com.bazaar.usuarios.service;

import com.bazaar.common.ResourceNotFoundException;
import com.bazaar.usuarios.dto.UsuarioRequest;
import com.bazaar.usuarios.entity.Usuario;
import com.bazaar.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario crear(UsuarioRequest request) {
        usuarioRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("El correo ya está registrado: " + request.getEmail());
        });

        // NOTA: password en texto plano hasta implementar RF-03 (Auth) con BCryptPasswordEncoder.
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail(),
                request.getPassword(), request.getTelefono());

        return usuarioRepository.save(usuario);
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
