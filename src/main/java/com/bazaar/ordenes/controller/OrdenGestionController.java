package com.bazaar.ordenes.controller;

import com.bazaar.ordenes.dto.OrdenResponse;
import com.bazaar.ordenes.service.OrdenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de despacho. No hay endpoint para marcar "pagada": eso solo lo
 * dispara el módulo pagos vía OrdenService.marcarComoPagada.
 *
 * TODO: restringir a vendedor/admin en SecurityConfig cuando los roles estén
 * conectados; hoy cualquier usuario autenticado podría llamarlos.
 */
@RestController
@RequestMapping("/api/ordenes/{ordenId}")
@RequiredArgsConstructor
public class OrdenGestionController {

    private final OrdenService ordenService;

    @PatchMapping("/enviar")
    public OrdenResponse enviar(@PathVariable Long ordenId) {
        return ordenService.marcarComoEnviada(ordenId);
    }

    @PatchMapping("/entregar")
    public OrdenResponse entregar(@PathVariable Long ordenId) {
        return ordenService.marcarComoEntregada(ordenId);
    }
}
