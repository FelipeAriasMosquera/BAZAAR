package com.bazaar.carrito.controller;

import com.bazaar.carrito.dto.ActualizarCantidadRequest;
import com.bazaar.carrito.dto.CarritoItemRequest;
import com.bazaar.carrito.dto.CarritoResponse;
import com.bazaar.carrito.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public CarritoResponse obtener(@PathVariable Long usuarioId) {
        return carritoService.obtenerCarrito(usuarioId);
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarItem(
            @PathVariable Long usuarioId,
            @Valid @RequestBody CarritoItemRequest request) {
        return ResponseEntity.ok(carritoService.agregarItem(usuarioId, request));
    }

    @PutMapping("/items/{itemId}")
    public CarritoResponse actualizarCantidad(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return carritoService.actualizarCantidad(usuarioId, itemId, request.cantidad());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.eliminarItem(usuarioId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciar(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
