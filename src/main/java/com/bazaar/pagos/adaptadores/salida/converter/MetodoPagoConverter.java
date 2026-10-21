package com.bazaar.pagos.adaptadores.salida.converter;

import com.bazaar.pagos.dominio.MetodoPago;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MetodoPagoConverter implements AttributeConverter<MetodoPago, String> {

    @Override
    public String convertToDatabaseColumn(MetodoPago attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public MetodoPago convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MetodoPago.desde(dbData);
    }
}
