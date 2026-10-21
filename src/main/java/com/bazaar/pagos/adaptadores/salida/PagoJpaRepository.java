package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.pagos.dominio.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio de Spring Data. Es un detalle interno del adaptador: el dominio solo ve PagoRepositoryPort. */
public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long> {

    List<PagoEntity> findByOrdenIdOrderByFechaDesc(Long ordenId);

    boolean existsByOrdenIdAndEstado(Long ordenId, EstadoPago estado);
}
