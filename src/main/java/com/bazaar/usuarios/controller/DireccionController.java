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
@RequestMapping("/api/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @PostMapping
    public ResponseEntity<DireccionResponse> crear(@Valid @RequestBody DireccionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DireccionResponse(direccionService.crear(request)));
    }

    @GetMapping
    public List<DireccionResponse> listar() {
        return direccionService.listar().stream().map(DireccionResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/{direccionId}")
    public DireccionResponse obtener(@PathVariable Long direccionId) {
        return new DireccionResponse(direccionService.obtener(direccionId));
    }

    @PutMapping("/{direccionId}")
    public DireccionResponse actualizar(
            @PathVariable Long direccionId,
            @Valid @RequestBody DireccionRequest request) {
        return new DireccionResponse(direccionService.actualizar(direccionId, request));
    }

    @PatchMapping("/{direccionId}/principal")
    public ResponseEntity<Void> marcarComoPrincipal(@PathVariable Long direccionId) {
        direccionService.marcarComoPrincipal(direccionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{direccionId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long direccionId) {
        direccionService.eliminar(direccionId);
        return ResponseEntity.noContent().build();
    }
}
