package com.bazaar.catalogo.service;

import com.bazaar.catalogo.entity.Categoria;
import com.bazaar.catalogo.repository.CategoriaRepository;
import com.bazaar.usuarios.repository.UsuarioRepository;

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
import com.bazaar.usuarios.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        verificarEsVendedor(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));
        
        TipoVenta tipoVenta = TipoVenta.desde(request.tipoVenta());
        validarReglasDePrecio(tipoVenta, request.precio());

        String slug = resolverSlug(request.slug(), request.nombre());
        if (productoRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe un producto con el slug '" + slug + "'");
        }

        Usuario vendedor = resolverVendedorAutenticado();
        Categoria categoria = resolverCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .vendedor(vendedor)
                .categoria(categoria)
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
        verificarPropietarioOAdmin(producto, Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));

        TipoVenta tipoVenta = TipoVenta.desde(request.tipoVenta());
        validarReglasDePrecio(tipoVenta, request.precio());

        String slug = resolverSlug(request.slug(), request.nombre());
        if (productoRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException("Ya existe un producto con el slug '" + slug + "'");
        }

        Categoria categoria = resolverCategoria(request.categoriaId());

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
        return productoRepository.findByVendedor_IdAndEstado(vendedorId, EstadoProducto.ACTIVO, pageable)
                .map(ProductoMapper::toResponse);
    }

    @Transactional
    public ProductoResponse agregarImagen(Long id, ProductoImagenRequest request) {
        Producto producto = buscarOFallar(id);
        verificarPropietarioOAdmin(producto, Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));

        producto.agregarImagen(ProductoImagen.builder()
                .url(request.url())
                .orden(request.orden() != null ? request.orden() : (short) producto.getImagenes().size())
                .build());
        return ProductoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse eliminarImagen(Long id, Long imagenId) {
        Producto producto = buscarOFallar(id);
        verificarPropietarioOAdmin(producto, Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));

        boolean eliminada = producto.getImagenes().removeIf(img -> img.getId().equals(imagenId));
        if (!eliminada) {
            throw new ResourceNotFoundException("La imagen " + imagenId + " no pertenece al producto " + id);
        }
        return ProductoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse reactivar(Long id) {
        Producto producto = buscarOFallar(id);
        verificarAdmin(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));

        if (producto.getEstado() != EstadoProducto.ELIMINADO) {
            throw new BusinessException(
                    "Solo se pueden reactivar productos en estado 'eliminado' (actual: "
                            + producto.getEstado().getValor() + ")");
        }
        producto.setEstado(EstadoProducto.ACTIVO);
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
        verificarPropietarioOAdmin(producto, Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()));
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

    private Usuario resolverVendedorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // tu JwtAuthFilter guarda el email como "username"
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email));
    }

    private Producto buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id " + id));
    }

    private String resolverSlug(String slugSolicitado, String nombre) {
        String base = (slugSolicitado != null && !slugSolicitado.isBlank()) ? slugSolicitado : nombre;
        return SlugUtil.generar(base);
    }

    private void verificarPropietarioOAdmin(Producto producto, Authentication auth) {
        boolean esDueño = producto.getVendedor().getEmail().equalsIgnoreCase(auth.getName());
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_admin"));

        if (!esDueño && !esAdmin) {
            throw new AccessDeniedException("No tienes permiso para modificar este producto");
        }
    }

    private void verificarAdmin(Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_admin"));

        if (!esAdmin) {
            throw new AccessDeniedException("Solo un administrador puede reactivar un producto");
        }
    }

    private void verificarEsVendedor(Authentication auth) {
        boolean esVendedor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_vendedor"));

        if (!esVendedor) {
            throw new AccessDeniedException("Solo los usuarios con rol vendedor pueden crear productos");
        }
    }

}
