package com.bazaar.notificaciones.entity;

import com.bazaar.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private boolean leido = false;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @PrePersist
    void alPersistir() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }
}
