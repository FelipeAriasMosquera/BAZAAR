package com.bazaar.resenas.entity;

import com.bazaar.catalogo.entity.Producto;
import com.bazaar.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "resenas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // SMALLINT en la BD (CHECK 1..5): Short para que ddl-auto=validate no se queje.
    @Column(nullable = false)
    private Short calificacion;

    @Column(columnDefinition = "text")
    private String comentario;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @PrePersist
    void alPersistir() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }
}
