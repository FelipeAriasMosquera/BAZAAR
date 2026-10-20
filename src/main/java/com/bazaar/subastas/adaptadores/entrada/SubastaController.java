package com.bazaar.subastas.adaptadores.entrada;

import com.bazaar.subastas.aplicacion.ConsultarSubastasUseCase;
import com.bazaar.subastas.aplicacion.RegistrarPujaUseCase;
import com.bazaar.subastas.dominio.Puja;
import com.bazaar.subastas.puertos.UsuarioPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
@RequiredArgsConstructor
public class SubastaController {

    private final RegistrarPujaUseCase registrarPuja;
    private final ConsultarSubastasUseCase consultar;
    private final UsuarioPort usuarioPort;

    @GetMapping
    public List<SubastaResponse> activas() {
        return consultar.listarActivas().stream().map(SubastaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public SubastaResponse detalle(@PathVariable Long id) {
        return SubastaResponse.de(consultar.obtener(id));
    }

    @GetMapping("/{id}/pujas")
    public List<Puja> historial(@PathVariable Long id) {
        return consultar.historial(id);
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