package com.bazaar.usuarios.controller;

import com.bazaar.usuarios.dto.DireccionRequest;
import com.bazaar.usuarios.dto.DireccionResponse;
import com.bazaar.usuarios.entity.Direccion;
import com.bazaar.usuarios.service.DireccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @PostMapping
    public ResponseEntity<DireccionResponse> crear(
            @PathVariable Long usuarioId,
            @Valid @RequestBody DireccionRequest request) {
        Direccion creada = direccionService.crear(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DireccionResponse(creada));
    }

    @GetMapping
    public List<DireccionResponse> listar(@PathVariable Long usuarioId) {
        return direccionService.listarPorUsuario(usuarioId).stream()
                .map(DireccionResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{direccionId}")
    public DireccionResponse obtener(@PathVariable Long usuarioId, @PathVariable Long direccionId) {
        return new DireccionResponse(direccionService.obtener(usuarioId, direccionId));
    }

    @PutMapping("/{direccionId}")
    public DireccionResponse actualizar(
            @PathVariable Long usuarioId,
            @PathVariable Long direccionId,
            @Valid @RequestBody DireccionRequest request) {
        return new DireccionResponse(direccionService.actualizar(usuarioId, direccionId, request));
    }

    @PatchMapping("/{direccionId}/principal")
    public ResponseEntity<Void> marcarComoPrincipal(
            @PathVariable Long usuarioId,
            @PathVariable Long direccionId) {
        direccionService.marcarComoPrincipal(usuarioId, direccionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{direccionId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long usuarioId, @PathVariable Long direccionId) {
        direccionService.eliminar(usuarioId, direccionId);
        return ResponseEntity.noContent().build();
    }
}
