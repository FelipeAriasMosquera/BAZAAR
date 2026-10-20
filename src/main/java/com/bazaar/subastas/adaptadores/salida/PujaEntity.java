package com.bazaar.subastas.adaptadores.salida;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pujas")
@Getter @Setter
public class PujaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "subasta_id") private Long subastaId;
    @Column(name = "usuario_id") private Long usuarioId;
    private BigDecimal monto;
    private OffsetDateTime fecha;
}