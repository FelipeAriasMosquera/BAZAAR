package com.bazaar.ordenes.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

/**
 * Guarda EstadoOrden como 'pendiente', 'pagada', ... (minúscula), que es lo
 * que exige el CHECK de la tabla ordenes. Se aplica explícitamente con
 * @Convert en Orden.estado, no es autoApply.
 */
@Converter
public class EstadoOrdenConverter implements AttributeConverter<EstadoOrden, String> {

    @Override
    public String convertToDatabaseColumn(EstadoOrden estado) {
        return estado == null ? null : estado.enBd();
    }

    @Override
    public EstadoOrden convertToEntityAttribute(String valor) {
        return valor == null ? null : EstadoOrden.valueOf(valor.toUpperCase(Locale.ROOT));
    }
}
