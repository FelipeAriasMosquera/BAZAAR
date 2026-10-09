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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final ProductoRepository productoRepository;

    public CarritoResponse obtenerCarrito(Long usuarioId) {
        return CarritoMapper.toResponse(obtenerOCrearCarrito(usuarioId));
    }

    @Transactional
    public CarritoResponse agregarItem(Long usuarioId, CarritoItemRequest request) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
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
    public CarritoResponse actualizarCantidad(Long usuarioId, Long itemId, Integer cantidad) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        CarritoItem item = carritoItemRepository.findByIdAndCarrito_Id(itemId, carrito.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El ítem " + itemId + " no pertenece al carrito del usuario " + usuarioId));

        validarStock(item.getProducto(), cantidad);
        item.setCantidad(cantidad);

        return CarritoMapper.toResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long usuarioId, Long itemId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        boolean eliminado = carrito.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!eliminado) {
            throw new ResourceNotFoundException(
                    "El ítem " + itemId + " no pertenece al carrito del usuario " + usuarioId);
        }
        return CarritoMapper.toResponse(carrito);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        obtenerOCrearCarrito(usuarioId).getItems().clear();
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> carritoRepository.save(Carrito.builder().usuarioId(usuarioId).build()));
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
}
