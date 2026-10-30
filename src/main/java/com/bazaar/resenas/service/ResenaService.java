package com.bazaar.resenas.service;

import com.bazaar.catalogo.entity.Producto;
import com.bazaar.catalogo.repository.ProductoRepository;
import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.ordenes.service.OrdenService;
import com.bazaar.resenas.dto.ResenaRequest;
import com.bazaar.resenas.dto.ResenaResponse;
import com.bazaar.resenas.dto.ResumenResenasResponse;
import com.bazaar.resenas.entity.Resena;
import com.bazaar.resenas.mapper.ResenaMapper;
import com.bazaar.resenas.repository.ResenaRepository;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Capas simples, igual que Productos/Carrito/Órdenes.
 * RF-32: calificar y comentar un producto comprado. RF-33: promedio por producto.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenService ordenService;

    // ------------------------------------------------------------------
    // Escritura (usuario autenticado)
    // ------------------------------------------------------------------

    @Transactional
    public ResenaResponse crear(Long productoId, ResenaRequest request) {
        Usuario usuario = usuarioAutenticado();
        Producto producto = buscarProducto(productoId);

        if (producto.getVendedor().getId().equals(usuario.getId())) {
            throw new BusinessException("No puedes reseñar tu propio producto");
        }
        if (!ordenService.usuarioCompro(usuario.getId(), productoId)) {
            throw new BusinessException("Solo puedes reseñar productos que hayas comprado y pagado");
        }
        if (resenaRepository.existsByProducto_IdAndUsuario_Id(productoId, usuario.getId())) {
            throw new BusinessException("Ya reseñaste este producto; puedes editar tu reseña");
        }

        Resena resena = Resena.builder()
                .producto(producto)
                .usuario(usuario)
                .calificacion(request.calificacion().shortValue())
                .comentario(limpiarComentario(request.comentario()))
                .build();

        try {
            return ResenaMapper.toResponse(resenaRepository.save(resena));
        } catch (DataIntegrityViolationException e) {
            // Dos peticiones simultáneas: la segunda choca con el índice único (producto_id, usuario_id).
            throw new BusinessException("Ya reseñaste este producto; puedes editar tu reseña");
        }
    }

    @Transactional
    public ResenaResponse actualizar(Long resenaId, ResenaRequest request) {
        Resena resena = buscarResena(resenaId);
        if (!resena.getUsuario().getId().equals(usuarioAutenticado().getId())) {
            throw new AccessDeniedException("Solo puedes editar tus propias reseñas");
        }

        resena.setCalificacion(request.calificacion().shortValue());
        resena.setComentario(limpiarComentario(request.comentario()));
        return ResenaMapper.toResponse(resena);
    }

    /** La puede borrar su autor o un administrador (moderación). */
    @Transactional
    public void eliminar(Long resenaId) {
        Resena resena = buscarResena(resenaId);
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());

        boolean esAutor = resena.getUsuario().getId().equals(usuarioAutenticado().getId());
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_admin"));

        if (!esAutor && !esAdmin) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta reseña");
        }
        resenaRepository.delete(resena);
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public Page<ResenaResponse> listarPorProducto(Long productoId, Pageable pageable) {
        buscarProducto(productoId);
        return resenaRepository.findByProducto_Id(productoId, pageable).map(ResenaMapper::toResponse);
    }

    public Page<ResenaResponse> listarMias(Pageable pageable) {
        return resenaRepository.findByUsuario_Id(usuarioAutenticado().getId(), pageable)
                .map(ResenaMapper::toResponse);
    }

    /** RF-33. */
    public ResumenResenasResponse resumen(Long productoId) {
        buscarProducto(productoId);
        ResenaRepository.ResumenProjection datos = resenaRepository.resumenPorProducto(productoId);

        long total = datos.getTotal() == null ? 0L : datos.getTotal();
        BigDecimal promedio = datos.getPromedio() == null
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(datos.getPromedio()).setScale(1, RoundingMode.HALF_UP);

        return new ResumenResenasResponse(productoId, promedio, total);
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private Usuario usuarioAutenticado() {
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email));
    }

    private Producto buscarProducto(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id " + productoId));
    }

    private Resena buscarResena(Long resenaId) {
        return resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id " + resenaId));
    }

    private String limpiarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) return null;
        return comentario.trim();
    }
}
