package com.bazaar.catalogo.service;

import com.bazaar.catalogo.entity.Categoria;
import com.bazaar.catalogo.repository.CategoriaRepository;
import com.bazaar.catalogo.dto.ProductoImagenRequest;
import com.bazaar.catalogo.dto.ProductoRequest;
import com.bazaar.catalogo.dto.ProductoResponse;
import com.bazaar.catalogo.entity.EstadoProducto;
import com.bazaar.catalogo.entity.Producto;
import com.bazaar.catalogo.entity.ProductoImagen;
import com.bazaar.catalogo.entity.TipoVenta;
import com.bazaar.catalogo.mapper.ProductoMapper;
import com.bazaar.catalogo.repository.ProductoRepository;
import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sin interfaz aparte: no hay (todavía) una segunda implementación de este
 * servicio, así que una interfaz CategoriaService-style sería boilerplate
 * sin beneficio real, como vimos con "categoria".
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        TipoVenta tipoVenta = TipoVenta.desde(request.tipoVenta());
        validarReglasDePrecio(tipoVenta, request.precio());

        String slug = resolverSlug(request.slug(), request.nombre());
        if (productoRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe un producto con el slug '" + slug + "'");
        }

        Categoria categoria = resolverCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .vendedorId(request.vendedorId())
                .categoria(categoria)
                .nombre(request.nombre())
                .slug(slug)
                .descripcion(request.descripcion())
                .tipoVenta(tipoVenta)
                .precio(tipoVenta == TipoVenta.FIJO ? request.precio() : null)
                .stock(tipoVenta == TipoVenta.FIJO ? (request.stock() != null ? request.stock() : 1) : null)
                .estado(EstadoProducto.ACTIVO)
                .build();

        agregarImagenes(producto, request.imagenes());

        return ProductoMapper.toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarOFallar(id);

        TipoVenta tipoVenta = TipoVenta.desde(request.tipoVenta());
        validarReglasDePrecio(tipoVenta, request.precio());

        String slug = resolverSlug(request.slug(), request.nombre());
        if (productoRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException("Ya existe un producto con el slug '" + slug + "'");
        }

        Categoria categoria = resolverCategoria(request.categoriaId());

        producto.setVendedorId(request.vendedorId());
        producto.setCategoria(categoria);
        producto.setNombre(request.nombre());
        producto.setSlug(slug);
        producto.setDescripcion(request.descripcion());
        producto.setTipoVenta(tipoVenta);
        producto.setPrecio(tipoVenta == TipoVenta.FIJO ? request.precio() : null);
        producto.setStock(tipoVenta == TipoVenta.FIJO ? request.stock() : null);

        if (request.imagenes() != null) {
            producto.limpiarImagenes();
            agregarImagenes(producto, request.imagenes());
        }

        return ProductoMapper.toResponse(producto);
    }

    public ProductoResponse obtenerPorId(Long id) {
        return ProductoMapper.toResponse(buscarOFallar(id));
    }

    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findByEstado(EstadoProducto.ACTIVO, pageable)
                .map(ProductoMapper::toResponse);
    }

    public Page<ProductoResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        return productoRepository.findByCategoria_IdAndEstado(categoriaId, EstadoProducto.ACTIVO, pageable)
                .map(ProductoMapper::toResponse);
    }

    public Page<ProductoResponse> listarPorVendedor(Long vendedorId, Pageable pageable) {
        return productoRepository.findByVendedorIdAndEstado(vendedorId, EstadoProducto.ACTIVO, pageable)
                .map(ProductoMapper::toResponse);
    }

    @Transactional
    public ProductoResponse agregarImagen(Long id, ProductoImagenRequest request) {
        Producto producto = buscarOFallar(id);
        producto.agregarImagen(ProductoImagen.builder()
                .url(request.url())
                .orden(request.orden() != null ? request.orden() : (short) producto.getImagenes().size())
                .build());
        return ProductoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse eliminarImagen(Long id, Long imagenId) {
        Producto producto = buscarOFallar(id);
        boolean eliminada = producto.getImagenes().removeIf(img -> img.getId().equals(imagenId));
        if (!eliminada) {
            throw new ResourceNotFoundException("La imagen " + imagenId + " no pertenece al producto " + id);
        }
        return ProductoMapper.toResponse(producto);
    }

    /**
     * Soft delete: tu propio schema ya contempla el estado "eliminado".
     * Borrar físicamente rompería el historial de orden_items, carrito_items
     * y resenas, que referencian producto_id sin ON DELETE CASCADE.
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarOFallar(id);
        producto.setEstado(EstadoProducto.ELIMINADO);
    }

    private void agregarImagenes(Producto producto, List<ProductoImagenRequest> imagenes) {
        if (imagenes == null) return;
        for (ProductoImagenRequest img : imagenes) {
            producto.agregarImagen(ProductoImagen.builder()
                    .url(img.url())
                    .orden(img.orden() != null ? img.orden() : (short) producto.getImagenes().size())
                    .build());
        }
    }

    private void validarReglasDePrecio(TipoVenta tipoVenta, BigDecimal precio) {
        if (tipoVenta == TipoVenta.FIJO && precio == null) {
            throw new BusinessException("Los productos de venta fija requieren un precio");
        }
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id " + categoriaId));
    }

    private Producto buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id " + id));
    }

    private String resolverSlug(String slugSolicitado, String nombre) {
        String base = (slugSolicitado != null && !slugSolicitado.isBlank()) ? slugSolicitado : nombre;
        return SlugUtil.generar(base);
    }
}
