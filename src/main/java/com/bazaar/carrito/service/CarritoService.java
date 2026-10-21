package com.bazaar.carrito.service;

import com.bazaar.carrito.dto.CarritoItemRequest;
import com.bazaar.carrito.dto.CarritoResponse;
import com.bazaar.carrito.entity.Carrito;
import com.bazaar.carrito.entity.CarritoItem;
import com.bazaar.carrito.mapper.CarritoMapper;
import com.bazaar.carrito.repository.CarritoItemRepository;
import com.bazaar.carrito.repository.CarritoRepository;
import com.bazaar.catalogo.entity.EstadoProducto;
import com.bazaar.catalogo.entity.Producto;
import com.bazaar.catalogo.entity.TipoVenta;
import com.bazaar.catalogo.repository.ProductoRepository;
import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoResponse obtenerCarrito() {
        return CarritoMapper.toResponse(obtenerOCrearCarrito());
    }

    public CarritoResponse agregarItem(CarritoItemRequest request) {
        Carrito carrito = obtenerOCrearCarrito();
        Producto producto = buscarProductoVendible(request.productoId());

        int cantidadFinal = carritoItemRepository
                .findByCarrito_IdAndProducto_Id(carrito.getId(), producto.getId())
                .map(existente -> existente.getCantidad() + request.cantidad())
                .orElse(request.cantidad());

        validarStock(producto, cantidadFinal);

        carritoItemRepository.findByCarrito_IdAndProducto_Id(carrito.getId(), producto.getId())
                .ifPresentOrElse(
                        existente -> existente.setCantidad(cantidadFinal),
                        () -> carrito.agregarItem(CarritoItem.builder()
                                .producto(producto)
                                .cantidad(cantidadFinal)
                                .build())
                );

        return CarritoMapper.toResponse(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoResponse actualizarCantidad(Long itemId, Integer cantidad) {
        Carrito carrito = obtenerOCrearCarrito();
        CarritoItem item = carritoItemRepository.findByIdAndCarrito_Id(itemId, carrito.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El ítem " + itemId + " no pertenece a tu carrito"));

        validarStock(item.getProducto(), cantidad);
        item.setCantidad(cantidad);

        return CarritoMapper.toResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long itemId) {
        Carrito carrito = obtenerOCrearCarrito();
        boolean eliminado = carrito.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!eliminado) {
            throw new ResourceNotFoundException("El ítem " + itemId + " no pertenece a tu carrito");
        }
        return CarritoMapper.toResponse(carrito);
    }

    @Transactional
    public void vaciarCarrito() {
        obtenerOCrearCarrito().getItems().clear();
    }
    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
                    return carritoRepository.save(Carrito.builder().usuario(usuario).build());
                });
    }

    /**
     * RF-23: solo productos de modalidad "fijo" entran al carrito; los de
     * subasta se ganan por puja, nunca se agregan por aquí.
     */
    private Producto buscarProductoVendible(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));

        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new BusinessException("El producto '" + producto.getNombre() + "' no está disponible");
        }

        if (producto.getTipoVenta() != TipoVenta.FIJO) {
            throw new BusinessException(
                    "El producto '" + producto.getNombre() + "' es de subasta y no se puede agregar al carrito");
        }

        return producto;
    }

    /** RF-24: valida disponibilidad antes de agregar/actualizar cantidad. */
    private void validarStock(Producto producto, int cantidadSolicitada) {
        Integer stock = producto.getStock();
        if (stock == null || stock < cantidadSolicitada) {
            throw new BusinessException(
                    "Stock insuficiente para '" + producto.getNombre() + "' (disponible: "
                            + (stock == null ? 0 : stock) + ")");
        }
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email));
    }

    private Carrito obtenerOCrearCarrito() {
        Usuario usuario = usuarioAutenticado();
        return carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> carritoRepository.save(Carrito.builder().usuario(usuario).build()));
    }
}
