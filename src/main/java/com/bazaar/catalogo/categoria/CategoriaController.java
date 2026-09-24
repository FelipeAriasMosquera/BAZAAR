package com.bazaar.catalogo.categoria;

import com.bazaar.catalogo.categoria.dto.CategoriaArbolResponse;
import com.bazaar.catalogo.categoria.dto.CategoriaRequest;
import com.bazaar.catalogo.categoria.dto.CategoriaResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión del catálogo de categorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse creada = categoriaService.crear(request);
        return ResponseEntity.created(URI.create("/api/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<CategoriaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(categoriaService.listar(pageable));
    }

    @GetMapping("/raiz")
    public ResponseEntity<List<CategoriaResponse>> listarRaiz() {
        return ResponseEntity.ok(categoriaService.listarRaiz());
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaResponse>> listarPorPadre(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.listarPorPadre(id));
    }

    @GetMapping("/arbol")
    public ResponseEntity<List<CategoriaArbolResponse>> obtenerArbol() {
        return ResponseEntity.ok(categoriaService.obtenerArbol());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
