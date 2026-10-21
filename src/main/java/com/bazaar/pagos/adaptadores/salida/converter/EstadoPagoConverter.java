package com.bazaar.pagos.adaptadores.salida.converter;

import com.bazaar.pagos.dominio.EstadoPago;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPagoConverter implements AttributeConverter<EstadoPago, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPago attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public EstadoPago convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoPago.desde(dbData);
    }
}
