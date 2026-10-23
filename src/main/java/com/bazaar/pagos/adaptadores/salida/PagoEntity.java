package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.pagos.dominio.EstadoPago;
import com.bazaar.pagos.dominio.MetodoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mapeo de la tabla "pagos" (ddl-auto=validate: debe coincidir con el script SQL).
 * Es un detalle de persistencia: el dominio trabaja con Pago, no con esta clase.
 *
 * ordenId es un Long y no una relación @ManyToOne porque todavía no existe la entidad
 * Orden; la FK a ordenes(id) la garantiza la base de datos.
 *
 * Los enums se convierten solos a su valor en minúscula con EstadoPagoConverter y
 * MetodoPagoConverter (autoApply), igual que EstadoProducto en catálogo.
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(nullable = false, length = 30)
    private MetodoPago metodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "referencia_pasarela", length = 150)
    private String referenciaPasarela;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @PrePersist
    void alPersistir() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagoEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

