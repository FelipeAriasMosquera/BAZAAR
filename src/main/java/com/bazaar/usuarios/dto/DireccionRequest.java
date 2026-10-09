package com.bazaar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DireccionRequest {

    @Size(max = 60)
    private String alias;

    @NotBlank
    @Size(max = 200)
    private String calle;

    @NotBlank
    @Size(max = 100)
    private String ciudad;

    @Size(max = 100)
    private String departamento;

    @NotBlank
    @Size(max = 100)
    private String pais;

    @Size(max = 20)
    private String codigoPostal;

    /** Opcional: si es null, se asume false salvo que sea la primera dirección del usuario. */
    private Boolean esPrincipal;
}
