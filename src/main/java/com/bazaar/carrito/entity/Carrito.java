package com.bazaar.carrito.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: cuando el módulo de usuarios exponga la entidad Usuario, cambiar
    // por un @ManyToOne, igual que quedó pendiente en Producto.vendedorId.
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CarritoItem> items = new ArrayList<>();

    @PrePersist
    void alPersistir() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    public void agregarItem(CarritoItem item) {
        item.setCarrito(this);
        items.add(item);
    }
}
