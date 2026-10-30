
package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReembolsarPagoUseCaseTest {

    private PagosFakes.Pagos pagos;
    private PagosFakes.Pasarela pasarela;
    private ReembolsarPagoUseCase useCase;

    private Authentication admin;
    private Authentication usuario;

    @BeforeEach
    void preparar() {
        pagos = new PagosFakes.Pagos();
        pasarela = new PagosFakes.Pasarela();
        useCase = new ReembolsarPagoUseCase(pagos, pasarela);

        admin = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        usuario = new UsernamePasswordAuthenticationToken(
                "usuario",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void administradorPuedeReembolsarUnPagoAprobado() {
        Pago aprobado = pagos.sembrar(
                1001L,
                "150000.00",
                EstadoPago.APROBADO,
                "REF-1"
        );

        Pago resultado = useCase.ejecutar(aprobado.getId(), admin);

        assertEquals(EstadoPago.REEMBOLSADO, resultado.getEstado());

        assertEquals(
                EstadoPago.REEMBOLSADO,
                pagos.buscarPorId(aprobado.getId())
                        .orElseThrow()
                        .getEstado()
        );

        assertEquals(
                List.of("REF-1"),
                pasarela.referenciasReembolsadas
        );

        assertEquals(
                new BigDecimal("150000.00"),
                pasarela.montosReembolsados.get(0)
        );
    }

    @Test
    void usuarioSinRolAdminNoPuedeReembolsar() {
        Pago aprobado = pagos.sembrar(
                1001L,
                "150000.00",
                EstadoPago.APROBADO,
                "REF-1"
        );

        assertThrows(
                AccessDeniedException.class,
                () -> useCase.ejecutar(aprobado.getId(), usuario)
        );

        // El pago no debe cambiar de estado.
        assertEquals(
                EstadoPago.APROBADO,
                pagos.buscarPorId(aprobado.getId())
                        .orElseThrow()
                        .getEstado()
        );

        // No se debe contactar a la pasarela.
        assertTrue(pasarela.referenciasReembolsadas.isEmpty());
    }

    @Test
    void unPagoQueNoEstaAprobadoNoSeReembolsa() {
        Pago pendiente = pagos.sembrar(
                1001L,
                "150000.00",
                EstadoPago.PENDIENTE,
                null
        );

        Pago rechazado = pagos.sembrar(
                1002L,
                "89900.50",
                EstadoPago.RECHAZADO,
                "REF-2"
        );

        assertThrows(
                BusinessException.class,
                () -> useCase.ejecutar(pendiente.getId(), admin)
        );

        assertThrows(
                BusinessException.class,
                () -> useCase.ejecutar(rechazado.getId(), admin)
        );

        assertTrue(pasarela.referenciasReembolsadas.isEmpty());
    }

    @Test
    void siLaPasarelaRechazaElReembolsoElPagoSigueAprobado() {
        Pago aprobado = pagos.sembrar(
                1001L,
                "150000.00",
                EstadoPago.APROBADO,
                "REF-1"
        );

        pasarela.aprobarReembolsos = false;

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.ejecutar(aprobado.getId(), admin)
        );

        assertTrue(
                ex.getMessage().contains("Reembolso no permitido")
        );

        assertEquals(
                EstadoPago.APROBADO,
                pagos.buscarPorId(aprobado.getId())
                        .orElseThrow()
                        .getEstado()
        );
    }

    @Test
    void unPagoInexistenteLanzaNotFound() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.ejecutar(9999L, admin)
        );
    }

    @Test
    void unPagoYaReembolsadoNoSePuedeReembolsarOtraVez() {
        Pago aprobado = pagos.sembrar(
                1001L,
                "150000.00",
                EstadoPago.APROBADO,
                "REF-1"
        );

        useCase.ejecutar(aprobado.getId(), admin);

        assertThrows(
                BusinessException.class,
                () -> useCase.ejecutar(aprobado.getId(), admin)
        );

        assertEquals(
                1,
                pasarela.referenciasReembolsadas.size()
        );
    }
}
