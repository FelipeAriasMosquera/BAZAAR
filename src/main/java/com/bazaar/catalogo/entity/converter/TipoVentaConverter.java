package com.bazaar.catalogo.entity.converter;

import com.bazaar.catalogo.entity.TipoVenta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Traduce el enum TipoVenta (FIJO, SUBASTA) a los valores exactos
 * que exige el CHECK de la tabla productos ('fijo', 'subasta').
 */
@Converter(autoApply = true)
public class TipoVentaConverter implements AttributeConverter<TipoVenta, String> {

    @Override
    public String convertToDatabaseColumn(TipoVenta attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public TipoVenta convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoVenta.desde(dbData);
    }
}
