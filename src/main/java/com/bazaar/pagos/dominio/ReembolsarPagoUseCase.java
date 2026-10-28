
package com.bazaar.pagos.dominio;

import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import com.bazaar.pagos.puertos.PasarelaPagoPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public class ReembolsarPagoUseCase {

    private final PagoRepositoryPort pagos;
    private final PasarelaPagoPort pasarela;

    public ReembolsarPagoUseCase(
            PagoRepositoryPort pagos,
            PasarelaPagoPort pasarela
    ) {
        this.pagos = pagos;
        this.pasarela = pasarela;
    }

    /**
     * Reembolsa un pago aprobado.
     * Solo un administrador puede ejecutar esta operación.
     */
    public Pago ejecutar(Long pagoId) {
        Pago pago = buscarPago(pagoId);

        pago.iniciarReembolso();
        pago = pagos.guardar(pago);   // reserva el pago ANTES de llamar a la pasarela

        ResultadoPasarela resultado = pasarela.reembolsar(
                pago.getReferenciaPasarela(),
                pago.getMonto());

        if (!resultado.exitoso()) {
            pago.revertirReembolso();
            pagos.guardar(pago);
            throw new BusinessException("La pasarela rechazó el reembolso: " + resultado.mensaje());
        }

        pago.confirmarReembolso();
        return pagos.guardar(pago);
    }

    private Pago buscarPago(Long pagoId) {
        return pagos.buscarPorId(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id " + pagoId));
    }

    /**
     * Comprueba que el usuario autenticado tenga el rol de administrador.
     */
    private void verificarAdmin(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación"
            );
        }

        boolean esAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equalsIgnoreCase(
                                authority.getAuthority()
                        )
                );

        if (!esAdmin) {
            throw new AccessDeniedException(
                    "Solo un administrador puede realizar reembolsos"
            );
        }
    }
}
