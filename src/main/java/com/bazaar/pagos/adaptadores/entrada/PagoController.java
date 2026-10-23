package com.bazaar.pagos.adaptadores.entrada;

import com.bazaar.pagos.adaptadores.entrada.dto.PagoRequest;
import com.bazaar.pagos.adaptadores.entrada.dto.PagoResponse;
import com.bazaar.pagos.dominio.ConsultarPagoUseCase;
import com.bazaar.pagos.dominio.MetodoPago;
import com.bazaar.pagos.dominio.Pago;
import com.bazaar.pagos.dominio.RegistrarPagoUseCase;
import com.bazaar.pagos.dominio.ReembolsarPagoUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de entrada HTTP: solo traduce request -> caso de uso -> response.
 * Ninguna regla de negocio vive aquí.
 *
 * Un cobro rechazado por la pasarela responde 201 con "estado": "rechazado" (el pago
 * se creó y quedó registrado); el cliente debe mirar el campo estado, no solo el HTTP.
 *
 * Pendiente: hoy SecurityConfig deja todo abierto. Cuando haya JWT/roles, el reembolso
 * debe ser solo para admin y el pago solo para el dueño de la orden (OrdenParaPago ya trae usuarioId).
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Pago de órdenes (pasarela y órdenes simuladas por ahora)")
public class PagoController {

    private final RegistrarPagoUseCase registrarPago;
    private final ConsultarPagoUseCase consultarPago;
    private final ReembolsarPagoUseCase reembolsarPago;

    @PostMapping
    public ResponseEntity<PagoResponse> pagar(@Valid @RequestBody PagoRequest request) {
        Pago pago = registrarPago.ejecutar(request.ordenId(), MetodoPago.desde(request.metodo()));
        return ResponseEntity.created(URI.create("/api/pagos/" + pago.getId()))
                .body(PagoMapper.toResponse(pago));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PagoMapper.toResponse(consultarPago.porId(id)));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<PagoResponse>> listarPorOrden(@PathVariable Long ordenId) {
        List<PagoResponse> pagos = consultarPago.porOrden(ordenId).stream()
                .map(PagoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pagos);
    }

    @PostMapping("/{id}/reembolso")
    public ResponseEntity<PagoResponse> reembolsar(@PathVariable Long id) {
        return ResponseEntity.ok(PagoMapper.toResponse(reembolsarPago.ejecutar(id)));
    }
}
