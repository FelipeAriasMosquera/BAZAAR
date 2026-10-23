package com.bazaar.subastas.adaptadores.salida;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subastas")
@Getter @Setter
public class SubastaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "producto_id", nullable = false) private Long productoId;
    @Column(name = "precio_inicial") private BigDecimal precioInicial;
    @Column(name = "precio_actual") private BigDecimal precioActual;
    @Column(name = "incremento_minimo") private BigDecimal incrementoMinimo;
    @Column(name = "precio_reserva") private BigDecimal precioReserva;
    @Column(name = "fecha_inicio") private OffsetDateTime fechaInicio;
    @Column(name = "fecha_fin") private OffsetDateTime fechaFin;
    private String estado;                          // en BD va en minúscula
    @Column(name = "ganador_id") private Long ganadorId;
}