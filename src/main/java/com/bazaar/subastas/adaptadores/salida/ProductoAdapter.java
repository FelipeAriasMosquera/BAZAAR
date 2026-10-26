package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.puertos.ProductoPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductoAdapter implements ProductoPort {

    private final EntityManager em;

    @Override
    public Optional<ProductoInfo> buscar(Long productoId) {
        List<?> filas = em.createNativeQuery(
                        "select vendedor_id, tipo_venta, estado from productos where id = :id")
                .setParameter("id", productoId)
                .getResultList();
        if (filas.isEmpty()) return Optional.empty();
        Object[] f = (Object[]) filas.get(0);
        return Optional.of(new ProductoInfo(((Number) f[0]).longValue(), (String) f[1], (String) f[2]));
    }
}