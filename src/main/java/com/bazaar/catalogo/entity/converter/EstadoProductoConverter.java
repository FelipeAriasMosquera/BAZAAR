package com.bazaar.catalogo.entity.converter;

import com.bazaar.catalogo.entity.EstadoProducto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoProductoConverter implements AttributeConverter<EstadoProducto, String> {

    @Override
    public String convertToDatabaseColumn(EstadoProducto attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public EstadoProducto convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoProducto.desde(dbData);
    }
}
