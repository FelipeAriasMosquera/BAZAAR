package com.bazaar.ordenes.service;

import com.bazaar.carrito.dto.CarritoItemResponse;
import com.bazaar.carrito.dto.CarritoResponse;
import com.bazaar.carrito.service.CarritoService;
import com.bazaar.catalogo.entity.EstadoProducto;
import com.bazaar.catalogo.entity.Producto;
import com.bazaar.catalogo.entity.TipoVenta;
import com.bazaar.catalogo.repository.ProductoRepository;
import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.ordenes.dto.OrdenResponse;
import com.bazaar.ordenes.entity.EstadoOrden;
import com.bazaar.ordenes.entity.Orden;
import com.bazaar.ordenes.entity.OrdenItem;
import com.bazaar.ordenes.mapper.OrdenMapper;
import com.bazaar.ordenes.repository.OrdenRepository;
import com.bazaar.ordenes.repository.ProductoStockRepository;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.DireccionRepository;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Capas simples (sin puertos), igual que ProductoService/CarritoService.
 *
 * Puntos de conexión con módulos hexagonales:
 *  - subastas: su OrdenAdapter llama a crearDesdeSubasta(...) al cerrar una subasta con ganador.
 *  - pagos:    su adaptador llama a marcarComoPagada(...) cuando la pasarela aprueba el pago.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoStockRepository productoStockRepository;
    private final ProductoRepository productoRepository;
    private final DireccionRepository direccionRepository;
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;

    @Value("${bazaar.ordenes.costo-envio:0}")
    private BigDecimal costoEnvio;

    // ------------------------------------------------------------------
    // Creación
    // ------------------------------------------------------------------

    /**
     * Checkout (RF-26, RF-27): convierte el carrito en una orden pendiente,
     * congela precios, descuenta stock y vacía el carrito. Todo en una sola
     * transacción: si un ítem falla, no queda nada a medias.
     */
    @Transactional
    public OrdenResponse crearDesdeCarrito(Long direccionId) {
        Usuario usuario = usuarioAutenticado();
        validarDireccion(usuario.getId(), direccionId);

        CarritoResponse carrito = carritoService.obtenerCarrito();
        if (carrito.items().isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        Orden orden = Orden.builder()
                .usuario(usuario)
                .direccionId(direccionId)
                .build();

        // Siempre en el mismo orden (por productoId): dos checkouts
        // concurrentes bloquean filas en el mismo orden y no se interbloquean.
        carrito.items().stream()
                .sorted(Comparator.comparing(CarritoItemResponse::productoId))
                .forEach(item -> orden.agregarItem(reservarItem(item)));

        orden.recalcularTotales(costoEnvio);
        Orden guardada = ordenRepository.save(orden);

        carritoService.vaciarCarrito();
        return OrdenMapper.toResponse(guardada);
    }

    /**
     * RF-20: orden pendiente de pago para el ganador de una subasta. Nace sin
     * dirección (el ganador la asigna con asignarDireccion) y no toca stock
     * (las subastas no manejan stock).
     */
    @Transactional
    public OrdenResponse crearDesdeSubasta(Long usuarioId, Long productoId, Long subastaId, BigDecimal montoFinal) {
        if (montoFinal == null || montoFinal.signum() <= 0) {
            throw new BusinessException("El monto final de la subasta debe ser mayor a 0");
        }
        if (ordenRepository.existsByItems_SubastaId(subastaId)) {
            throw new BusinessException("La subasta " + subastaId + " ya tiene una orden generada");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));

        Orden orden = Orden.builder().usuario(usuarioRepository.getReferenceById(usuarioId)).build();
        orden.agregarItem(OrdenItem.builder()
                .producto(producto)
                .subastaId(subastaId)
                .cantidad(1)
                .precioUnitario(montoFinal)
                .build());
        orden.recalcularTotales(costoEnvio);

        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public OrdenResponse obtener(Long ordenId) {
        return OrdenMapper.toResponse(buscarDeUsuario(usuarioAutenticado().getId(), ordenId));
    }

    public Page<OrdenResponse> listarPorUsuario(Pageable pageable) {
        return ordenRepository.findByUsuarioId(usuarioAutenticado().getId(), pageable)
                .map(OrdenMapper::toResponse);
    }

    // ------------------------------------------------------------------
    // Acciones del comprador
    // ------------------------------------------------------------------

    @Transactional
    public OrdenResponse asignarDireccion(Long ordenId, Long direccionId) {
        Long usuarioId = usuarioAutenticado().getId();
        Orden orden = buscarDeUsuario(usuarioId, ordenId);
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new BusinessException("Solo se puede cambiar la dirección de una orden pendiente");
        }
        validarDireccion(usuarioId, direccionId);

        orden.setDireccionId(direccionId);
        return OrdenMapper.toResponse(orden);
    }

    /** Cancela una orden pendiente y repone el stock de sus ítems de venta directa. */
    @Transactional
    public OrdenResponse cancelar(Long ordenId) {
        Orden orden = buscarDeUsuario(usuarioAutenticado().getId(), ordenId);
        transicionar(orden, EstadoOrden.CANCELADA);

        orden.getItems().stream()
                .filter(item -> item.getSubastaId() == null)
                .sorted(Comparator.comparing((OrdenItem item) -> item.getProducto().getId()))
                .forEach(item -> productoStockRepository.reponerStock(
                        item.getProducto().getId(), item.getCantidad()));

        return OrdenMapper.toResponse(orden);
    }

    // ------------------------------------------------------------------
    // Transiciones disparadas por otros módulos / gestión
    // ------------------------------------------------------------------

    /** Lo llama el módulo pagos cuando el pago queda aprobado. */
    @Transactional
    public OrdenResponse marcarComoPagada(Long ordenId) {
        Orden orden = buscarPorId(ordenId);
        transicionar(orden, EstadoOrden.PAGADA);
        return OrdenMapper.toResponse(orden);
    }

    @Transactional
    public OrdenResponse marcarComoEnviada(Long ordenId) {
        Orden orden = buscarPorId(ordenId);
        validarTransicion(orden, EstadoOrden.ENVIADA);
        if (orden.getDireccionId() == null) {
            throw new BusinessException("La orden no tiene dirección de envío");
        }
        orden.setEstado(EstadoOrden.ENVIADA);
        return OrdenMapper.toResponse(orden);
    }

    @Transactional
    public OrdenResponse marcarComoEntregada(Long ordenId) {
        Orden orden = buscarPorId(ordenId);
        transicionar(orden, EstadoOrden.ENTREGADA);
        return OrdenMapper.toResponse(orden);
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    /** Valida el producto, descuenta stock de forma atómica y arma el ítem con el precio vigente. */
    private OrdenItem reservarItem(CarritoItemResponse item) {
        Producto producto = productoRepository.findById(item.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.productoId()));

        if (producto.getEstado() != EstadoProducto.ACTIVO || producto.getTipoVenta() != TipoVenta.FIJO) {
            throw new BusinessException("El producto '" + producto.getNombre() + "' ya no está disponible");
        }

        if (productoStockRepository.descontarStock(producto.getId(), item.cantidad()) == 0) {
            throw new BusinessException("Stock insuficiente para '" + producto.getNombre() + "'");
        }

        return OrdenItem.builder()
                .producto(producto)
                .cantidad(item.cantidad())
                .precioUnitario(producto.getPrecio())
                .build();
    }

    private void validarDireccion(Long usuarioId, Long direccionId) {
        direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La dirección " + direccionId + " no pertenece al usuario " + usuarioId));
    }

    private void validarTransicion(Orden orden, EstadoOrden destino) {
        if (!orden.getEstado().permiteTransicionA(destino)) {
            throw new BusinessException("No se puede pasar una orden de '"
                    + orden.getEstado().enBd() + "' a '" + destino.enBd() + "'");
        }
    }

    private void transicionar(Orden orden, EstadoOrden destino) {
        validarTransicion(orden, destino);
        orden.setEstado(destino);
    }

    private Orden buscarPorId(Long ordenId) {
        return ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + ordenId));
    }

    private Orden buscarDeUsuario(Long usuarioId, Long ordenId) {
        return ordenRepository.findByIdAndUsuarioId(ordenId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La orden " + ordenId + " no pertenece al usuario " + usuarioId));
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email));
    }

    /** Lo usa Reseñas: ¿el usuario ya compró y pagó este producto? */
    public boolean usuarioCompro(Long usuarioId, Long productoId) {
        return ordenRepository.existeCompra(usuarioId, productoId,
                List.of(EstadoOrden.PAGADA, EstadoOrden.ENVIADA, EstadoOrden.ENTREGADA));
    }
}
