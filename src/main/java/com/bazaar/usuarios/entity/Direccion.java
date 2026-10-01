package com.bazaar.usuarios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 60)
    private String alias;

    @Column(nullable = false, length = 200)
    private String calle;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String pais;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal = false;
}
