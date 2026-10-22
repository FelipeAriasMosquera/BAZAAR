package com.bazaar.ordenes.entity;

import com.bazaar.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Raíz del agregado Orden. OrdenItem solo existe a través de esta clase
 * (sin repositorio propio), igual que ProductoImagen con Producto.
 */
@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Nullable: una orden creada por ganar una subasta nace sin dirección y
    // el ganador la asigna después.
    @Column(name = "direccion_id")
    private Long direccionId;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal envio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Convert(converter = EstadoOrdenConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<OrdenItem> items = new ArrayList<>();

    @PrePersist
    void alPersistir() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }

    public void agregarItem(OrdenItem item) {
        item.setOrden(this);
        items.add(item);
    }

    public void recalcularTotales(BigDecimal costoEnvio) {
        this.subtotal = items.stream()
                .map(OrdenItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.envio = costoEnvio;
        this.total = subtotal.add(envio);
    }
}
