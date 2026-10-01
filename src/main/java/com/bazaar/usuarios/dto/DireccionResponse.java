package com.bazaar.usuarios.dto;

import com.bazaar.usuarios.entity.Direccion;
import lombok.Getter;

@Getter
public class DireccionResponse {

    private final Long id;
    private final Long usuarioId;
    private final String alias;
    private final String calle;
    private final String ciudad;
    private final String departamento;
    private final String pais;
    private final String codigoPostal;
    private final boolean esPrincipal;

    public DireccionResponse(Direccion d) {
        this.id = d.getId();
        this.usuarioId = d.getUsuario().getId();
        this.alias = d.getAlias();
        this.calle = d.getCalle();
        this.ciudad = d.getCiudad();
        this.departamento = d.getDepartamento();
        this.pais = d.getPais();
        this.codigoPostal = d.getCodigoPostal();
        this.esPrincipal = d.isEsPrincipal();
    }
}
