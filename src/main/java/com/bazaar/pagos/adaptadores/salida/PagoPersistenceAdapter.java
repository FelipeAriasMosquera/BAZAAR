package com.bazaar.pagos.adaptadores.salida;

import com.bazaar.pagos.dominio.EstadoPago;
import com.bazaar.pagos.dominio.Pago;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el puerto de persistencia con Spring Data JPA. Traduce entre el Pago de
 * dominio y la PagoEntity para que el dominio nunca vea anotaciones de JPA.
 */
@Component
@RequiredArgsConstructor
public class PagoPersistenceAdapter implements PagoRepositoryPort {

    private final PagoJpaRepository jpaRepository;

    @Override
    public Pago guardar(Pago pago) {
        return aDominio(jpaRepository.save(aEntidad(pago)));
    }

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(PagoPersistenceAdapter::aDominio);
    }

    @Override
    public List<Pago> buscarPorOrden(Long ordenId) {
        return jpaRepository.findByOrdenIdOrderByFechaDesc(ordenId).stream()
                .map(PagoPersistenceAdapter::aDominio)
                .toList();
    }

    @Override
    public boolean existePagoVigente(Long ordenId) {
        return jpaRepository.existsByOrdenIdAndEstado(ordenId, EstadoPago.PENDIENTE)
                || jpaRepository.existsByOrdenIdAndEstado(ordenId, EstadoPago.APROBADO);
    }

    private static PagoEntity aEntidad(Pago pago) {
        return PagoEntity.builder()
                .id(pago.getId())
                .ordenId(pago.getOrdenId())
                .metodo(pago.getMetodo())
                .monto(pago.getMonto())
                .estado(pago.getEstado())
                .referenciaPasarela(pago.getReferenciaPasarela())
                .fecha(pago.getFecha())
                .build();
    }

    private static Pago aDominio(PagoEntity entidad) {
        return Pago.reconstruir(
                entidad.getId(),
                entidad.getOrdenId(),
                entidad.getMetodo(),
                entidad.getMonto(),
                entidad.getEstado(),
                entidad.getReferenciaPasarela(),
                entidad.getFecha());
    }
}
