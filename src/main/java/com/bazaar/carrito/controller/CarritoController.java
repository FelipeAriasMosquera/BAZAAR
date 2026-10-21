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
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public CarritoResponse obtener() {
        return carritoService.obtenerCarrito();
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarItem(@Valid @RequestBody CarritoItemRequest request) {
        return ResponseEntity.ok(carritoService.agregarItem(request));
    }

    @PutMapping("/items/{itemId}")
    public CarritoResponse actualizarCantidad(
            @PathVariable Long itemId,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return carritoService.actualizarCantidad(itemId, request.cantidad());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponse> eliminarItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.eliminarItem(itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciar() {
        carritoService.vaciarCarrito();
        return ResponseEntity.noContent().build();
    }
}
