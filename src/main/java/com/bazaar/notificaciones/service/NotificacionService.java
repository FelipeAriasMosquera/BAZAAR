package com.bazaar.notificaciones.service;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.notificaciones.dto.ConteoNoLeidasResponse;
import com.bazaar.notificaciones.dto.MarcadasResponse;
import com.bazaar.notificaciones.dto.NotificacionResponse;
import com.bazaar.notificaciones.entity.Notificacion;
import com.bazaar.notificaciones.entity.TipoNotificacion;
import com.bazaar.notificaciones.mapper.NotificacionMapper;
import com.bazaar.notificaciones.repository.NotificacionRepository;
import com.bazaar.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Notificaciones dentro de la app (tabla notificaciones).
 *
 * Dos usos:
 *  - Otros módulos (subastas, órdenes, pagos) llaman a crear(...) para avisar a un usuario.
 *    No requiere usuario autenticado: el destinatario se pasa por id.
 *  - El usuario autenticado consulta y marca como leídas SUS notificaciones
 *    (el usuario sale del token).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionService {

    private static final int MAX_MENSAJE = 300;

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    // ------------------------------------------------------------------
    // API para otros módulos
    // ------------------------------------------------------------------

    /**
     * Crea un aviso para el usuario. Se une a la transacción de quien llama: si esa operación
     * se revierte, el aviso tampoco queda guardado (no se avisa de algo que no ocurrió).
     */
    @Transactional
    public void crear(Long usuarioId, TipoNotificacion tipo, String mensaje) {
        if (usuarioId == null || tipo == null || mensaje == null || mensaje.isBlank()) {
            throw new BusinessException("Una notificación requiere usuario, tipo y mensaje");
        }

        notificacionRepository.save(Notificacion.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .tipo(tipo)
                .mensaje(recortar(mensaje.trim()))
                .build());
    }

    // ------------------------------------------------------------------
    // Acciones del usuario autenticado
    // ------------------------------------------------------------------

    public Page<NotificacionResponse> listar(boolean soloNoLeidas, Pageable pageable) {
        Long usuarioId = usuarioAutenticadoId();
        Page<Notificacion> pagina = soloNoLeidas
                ? notificacionRepository.findByUsuarioIdAndLeidoFalse(usuarioId, pageable)
                : notificacionRepository.findByUsuarioId(usuarioId, pageable);
        return pagina.map(NotificacionMapper::toResponse);
    }

    public ConteoNoLeidasResponse contarNoLeidas() {
        return new ConteoNoLeidasResponse(
                notificacionRepository.countByUsuarioIdAndLeidoFalse(usuarioAutenticadoId()));
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository
                .findByIdAndUsuarioId(notificacionId, usuarioAutenticadoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación no encontrada con id " + notificacionId));

        notificacion.setLeido(true);
        return NotificacionMapper.toResponse(notificacion);
    }

    @Transactional
    public MarcadasResponse marcarTodasComoLeidas() {
        return new MarcadasResponse(notificacionRepository.marcarTodasComoLeidas(usuarioAutenticadoId()));
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private Long usuarioAutenticadoId() {
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email))
                .getId();
    }

    /** La columna es VARCHAR(300): un mensaje más largo haría fallar el INSERT. */
    private String recortar(String mensaje) {
        return mensaje.length() <= MAX_MENSAJE ? mensaje : mensaje.substring(0, MAX_MENSAJE - 3) + "...";
    }
}
