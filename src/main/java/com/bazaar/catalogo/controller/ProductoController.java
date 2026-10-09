package com.bazaar.catalogo.controller;

import com.bazaar.catalogo.dto.ProductoImagenRequest;
import com.bazaar.catalogo.dto.ProductoRequest;
import com.bazaar.catalogo.dto.ProductoResponse;
import com.bazaar.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(pageable));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Page<ProductoResponse>> listarPorCategoria(@PathVariable Long categoriaId,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(productoService.listarPorCategoria(categoriaId, pageable));
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<Page<ProductoResponse>> listarPorVendedor(@PathVariable Long vendedorId,
                                                                    Pageable pageable) {
        return ResponseEntity.ok(productoService.listarPorVendedor(vendedorId, pageable));
    }

    @PostMapping("/{id}/imagenes")
    public ResponseEntity<ProductoResponse> agregarImagen(@PathVariable Long id,
                                                          @Valid @RequestBody ProductoImagenRequest request) {
        return ResponseEntity.ok(productoService.agregarImagen(id, request));
    }

    @DeleteMapping("/{id}/imagenes/{imagenId}")
    public ResponseEntity<ProductoResponse> eliminarImagen(@PathVariable Long id, @PathVariable Long imagenId) {
        return ResponseEntity.ok(productoService.eliminarImagen(id, imagenId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
