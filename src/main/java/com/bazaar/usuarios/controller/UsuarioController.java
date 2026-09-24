package com.bazaar.usuarios.controller;

import com.bazaar.usuarios.dto.UsuarioRequest;
import com.bazaar.usuarios.dto.UsuarioResponse;
import com.bazaar.usuarios.entity.Usuario;
import com.bazaar.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller mínimo: solo lo necesario para crear/consultar un usuario de
 * prueba y así poder probar el módulo de Direcciones. El CRUD completo de
 * Usuarios (RF-01, RF-02, RF-05) se aborda como tarea dedicada.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario creado = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponse(creado));
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return new UsuarioResponse(usuarioService.obtener(id));
    }
}
