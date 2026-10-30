package com.bazaar.notificaciones.controller;

import com.bazaar.notificaciones.dto.ConteoNoLeidasResponse;
import com.bazaar.notificaciones.dto.MarcadasResponse;
import com.bazaar.notificaciones.dto.NotificacionResponse;
import com.bazaar.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Las notificaciones del usuario autenticado. No hay endpoint para crearlas:
 * las generan los otros módulos a través de NotificacionService.crear(...).
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public Page<NotificacionResponse> listar(
            @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificacionService.listar(soloNoLeidas, pageable);
    }

    @GetMapping("/no-leidas/conteo")
    public ConteoNoLeidasResponse contarNoLeidas() {
        return notificacionService.contarNoLeidas();
    }

    @PatchMapping("/{notificacionId}/leida")
    public NotificacionResponse marcarComoLeida(@PathVariable Long notificacionId) {
        return notificacionService.marcarComoLeida(notificacionId);
    }

    @PatchMapping("/leidas")
    public MarcadasResponse marcarTodasComoLeidas() {
        return notificacionService.marcarTodasComoLeidas();
    }
}
