package com.bazaar.pagos.dominio;

/** Respuesta de la pasarela a un cobro o a un reembolso. */
public record ResultadoPasarela(boolean exitoso, String referencia, String mensaje) {

    public static ResultadoPasarela aprobado(String referencia) {
        return new ResultadoPasarela(true, referencia, null);
    }

    public static ResultadoPasarela rechazado(String referencia, String mensaje) {
        return new ResultadoPasarela(false, referencia, mensaje);
    }
}
