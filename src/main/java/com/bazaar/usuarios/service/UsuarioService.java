package com.bazaar.usuarios.service;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.security.JwtService;
import com.bazaar.usuarios.dto.*;
import com.bazaar.usuarios.model.Rol;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono());
        usuario.setRoles(new HashSet<>(Set.of(Rol.COMPRADOR))); // todos entran como comprador

        usuario = usuarioRepository.save(usuario);
        return aResponse(usuario);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new BusinessException("Esta cuenta ha sido desactivada");
        }

        String rolesClaim = usuario.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        String token = jwtService.generarToken(usuario.getEmail(), rolesClaim);
        return new LoginResponse(token);
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return aResponse(usuario);
    }

    public UsuarioResponse obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return aResponse(usuario);
    }

    public UsuarioResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(request.nombre());
        usuario.setTelefono(request.telefono());

        usuario = usuarioRepository.save(usuario);
        return aResponse(usuario);
    }

    public void cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    // Cualquier comprador puede convertirse también en vendedor
    public UsuarioResponse convertirseEnVendedor(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.getRoles().add(Rol.VENDEDOR);
        usuario = usuarioRepository.save(usuario);
        return aResponse(usuario);
    }

    // Exclusivo admin: reemplaza TODOS los roles de un usuario (incluye poder dar/quitar ADMIN)
    public UsuarioResponse cambiarRoles(Long id, Set<Rol> nuevosRoles) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setRoles(new HashSet<>(nuevosRoles));
        usuario = usuarioRepository.save(usuario);
        return aResponse(usuario);
    }

    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public void reactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    private UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(), u.getRoles());
    }



}