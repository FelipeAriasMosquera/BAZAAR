package com.bazaar.usuarios.controller;

import com.bazaar.usuarios.dto.*;
import com.bazaar.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // --- Sobre el propio usuario autenticado ---

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil(Authentication auth) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(auth.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            Authentication auth, @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(auth.getName(), request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(
            Authentication auth, @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(auth.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/vendedor")
    public ResponseEntity<UsuarioResponse> convertirmeEnVendedor(Authentication auth) {
        return ResponseEntity.ok(usuarioService.convertirseEnVendedor(auth.getName()));
    }

    // --- Exclusivas de ADMIN ---

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarRoles(
            @PathVariable Long id, @Valid @RequestBody CambiarRolesRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarRoles(id, request.roles()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        usuarioService.reactivar(id);
        return ResponseEntity.noContent().build();
    }

}