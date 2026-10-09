package com.bazaar.catalogo.entity;

import com.bazaar.catalogo.entity.Categoria;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Raíz del agregado Producto. ProductoImagen solo existe a través de este
 * agregado (sin repositorio propio): se agrega/quita vía los métodos de
 * esta clase, nunca directamente.
 */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: cuando exista la entidad Usuario (módulo usuarios), cambiar
    // este campo por un @ManyToOne hacia ella.
    @Column(name = "vendedor_id", nullable = false)
    private Long vendedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_venta", nullable = false, length = 10)
    private TipoVenta tipoVenta;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    private Integer stock;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoImagen> imagenes = new ArrayList<>();

    @PrePersist
    void alPersistir() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    public void agregarImagen(ProductoImagen imagen) {
        imagen.setProducto(this);
        imagenes.add(imagen);
    }

    public void limpiarImagenes() {
        imagenes.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
