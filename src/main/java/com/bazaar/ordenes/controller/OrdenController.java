package com.bazaar.ordenes.controller;

import com.bazaar.ordenes.dto.DireccionOrdenRequest;
import com.bazaar.ordenes.dto.OrdenResponse;
import com.bazaar.ordenes.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Acciones del comprador sobre sus órdenes. Misma convención que carrito:
 * usuarioId en la ruta mientras no se lea del token.
 */
@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    /** Checkout: crea la orden a partir del carrito del usuario. */
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody DireccionOrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenService.crearDesdeCarrito(request.direccionId()));
    }

    @GetMapping
    public Page<OrdenResponse> listar(
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ordenService.listarPorUsuario(pageable);
    }

    @GetMapping("/{ordenId}")
    public OrdenResponse obtener(@PathVariable Long ordenId) {
        return ordenService.obtener(ordenId);
    }

    @PatchMapping("/{ordenId}/direccion")
    public OrdenResponse asignarDireccion(
            @PathVariable Long ordenId,
            @Valid @RequestBody DireccionOrdenRequest request) {
        return ordenService.asignarDireccion(ordenId, request.direccionId());
    }

    @PatchMapping("/{ordenId}/cancelar")
    public OrdenResponse cancelar(@PathVariable Long ordenId) {
        return ordenService.cancelar(ordenId);
    }
}
