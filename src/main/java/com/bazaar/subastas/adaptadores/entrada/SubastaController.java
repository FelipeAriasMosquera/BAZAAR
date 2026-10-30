package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.subastas.aplicacion.ConsultarSubastasUseCase;
import com.bazaar.subastas.aplicacion.CrearSubastaUseCase;
import com.bazaar.subastas.aplicacion.RegistrarPujaUseCase;
import com.bazaar.subastas.dominio.Puja;
import com.bazaar.subastas.dominio.Subasta;
import com.bazaar.subastas.puertos.UsuarioPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
@RequiredArgsConstructor
public class SubastaController {

    private final RegistrarPujaUseCase registrarPuja;
    private final ConsultarSubastasUseCase consultar;
    private final CrearSubastaUseCase crearSubasta;
    private final UsuarioPort usuarioPort;

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<SubastaResponse> crear(@Valid @RequestBody CrearSubastaRequest r,
                                                 Authentication auth) {
        Long usuarioId = usuarioPort.obtenerIdPorEmail(auth.getName());
        Subasta s = crearSubasta.ejecutar(usuarioId, r.productoId(), r.precioInicial(),
                r.incrementoMinimo(), r.precioReserva(), r.fechaInicio(), r.fechaFin());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubastaResponse.de(s));
    }

    @GetMapping
    public List<SubastaResponse> activas() {
        return consultar.listarActivas().stream().map(SubastaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public SubastaResponse detalle(@PathVariable Long id) {
        return SubastaResponse.de(consultar.obtener(id));
    }

    @GetMapping("/{id}/pujas")
    public List<PujaResponse> historial(@PathVariable Long id, Authentication auth) {
        Long yo = usuarioPort.obtenerIdPorEmail(auth.getName());
        return consultar.historialDetallado(id).stream()
                .map(d -> PujaResponse.de(d, yo))
                .toList();
    }

    @PostMapping("/{id}/pujas")
    public ResponseEntity<Puja> pujar(@PathVariable Long id,
                                      @Valid @RequestBody PujaRequest request,
                                      Authentication auth) {
        Long usuarioId = usuarioPort.obtenerIdPorEmail(auth.getName());
        Puja puja = registrarPuja.ejecutar(id, usuarioId, request.monto());
        return ResponseEntity.status(HttpStatus.CREATED).body(puja);
    }
}