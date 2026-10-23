package com.bazaar.pagos.puertos;

import com.bazaar.pagos.dominio.ResultadoPasarela;
import com.bazaar.pagos.dominio.SolicitudCobro;

import java.math.BigDecimal;

/**
 * Puerto de salida hacia la pasarela de pagos (Wompi, PayU, Mercado Pago...).
 * Hoy lo implementa una pasarela simulada; cambiar de proveedor es escribir otro
 * adaptador, sin tocar el dominio.
 */
public interface PasarelaPagoPort {

    ResultadoPasarela cobrar(SolicitudCobro solicitud);

    /** referenciaPago es la referencia que devolvió la pasarela al aprobar el cobro. */
    ResultadoPasarela reembolsar(String referenciaPago, BigDecimal monto);
}
