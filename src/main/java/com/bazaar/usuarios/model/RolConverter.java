package com.bazaar.usuarios.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter implements AttributeConverter<Rol, String> {

    @Override
    public String convertToDatabaseColumn(Rol rol) {
        return rol == null ? null : rol.name().toLowerCase();
    }

    @Override
    public Rol convertToEntityAttribute(String valor) {
        return valor == null ? null : Rol.valueOf(valor.toUpperCase());
    }
}