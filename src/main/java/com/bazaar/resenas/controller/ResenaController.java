package com.bazaar.resenas.controller;

import com.bazaar.resenas.dto.ResenaRequest;
import com.bazaar.resenas.dto.ResenaResponse;
import com.bazaar.resenas.dto.ResumenResenasResponse;
import com.bazaar.resenas.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** El usuario siempre sale del token; ninguna ruta lleva usuarioId. */
@RestController
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    @PostMapping("/api/productos/{productoId}/resenas")
    public ResponseEntity<ResenaResponse> crear(
            @PathVariable Long productoId,
            @Valid @RequestBody ResenaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(productoId, request));
    }

    @GetMapping("/api/productos/{productoId}/resenas")
    public Page<ResenaResponse> listarPorProducto(
            @PathVariable Long productoId,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return resenaService.listarPorProducto(productoId, pageable);
    }

    @GetMapping("/api/productos/{productoId}/resenas/resumen")
    public ResumenResenasResponse resumen(@PathVariable Long productoId) {
        return resenaService.resumen(productoId);
    }

    @GetMapping("/api/resenas/mias")
    public Page<ResenaResponse> listarMias(
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return resenaService.listarMias(pageable);
    }

    @PutMapping("/api/resenas/{resenaId}")
    public ResenaResponse actualizar(
            @PathVariable Long resenaId,
            @Valid @RequestBody ResenaRequest request) {
        return resenaService.actualizar(resenaId, request);
    }

    @DeleteMapping("/api/resenas/{resenaId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long resenaId) {
        resenaService.eliminar(resenaId);
        return ResponseEntity.noContent().build();
    }
}
