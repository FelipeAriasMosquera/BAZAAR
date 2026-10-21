package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.pagos.dominio.ResultadoPasarela;
import com.bazaar.pagos.dominio.SolicitudCobro;
import com.bazaar.pagos.puertos.PasarelaPagoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pasarela de mentira para desarrollo: no llama a ningún servicio externo.
 * Regla simple y predecible para poder probar los dos caminos:
 *   - monto <= pagos.pasarela.simulada.monto-maximo  -> aprobado
 *   - monto >  pagos.pasarela.simulada.monto-maximo  -> rechazado ("fondos insuficientes")
 * El máximo es 5.000.000 por defecto; se cambia en application.properties.
 *
 * Para usar una pasarela real (Wompi, PayU...) se escribe otro adaptador que implemente
 * PasarelaPagoPort y se borra este.
 */
@Component
public class PasarelaSimuladaAdapter implements PasarelaPagoPort {

    private final BigDecimal montoMaximo;

    public PasarelaSimuladaAdapter(
            @Value("${pagos.pasarela.simulada.monto-maximo:5000000}") BigDecimal montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    @Override
    public ResultadoPasarela cobrar(SolicitudCobro solicitud) {
        String referencia = "SIM-" + UUID.randomUUID();
        if (solicitud.monto().compareTo(montoMaximo) > 0) {
            return ResultadoPasarela.rechazado(referencia, "Fondos insuficientes (simulado)");
        }
        return ResultadoPasarela.aprobado(referencia);
    }

    @Override
    public ResultadoPasarela reembolsar(String referenciaPago, BigDecimal monto) {
        return ResultadoPasarela.aprobado("SIM-RF-" + UUID.randomUUID());
    }
}
