package com.bazaar.subastas.aplicacion;

import com.bazaar.subastas.dominio.*;
import com.bazaar.subastas.puertos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CrearSubastaUseCase {

    private final SubastaRepositoryPort subastas;
    private final ProductoPort productos;

    @Transactional
    public Subasta ejecutar(Long usuarioId, Long productoId, BigDecimal precioInicial,
                            BigDecimal incrementoMinimo, BigDecimal precioReserva,
                            OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {

        ProductoPort.ProductoInfo producto = productos.buscar(productoId)
                .orElseThrow(() -> new SubastaInvalidaException("El producto no existe"));

        if (!producto.vendedorId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("Solo el dueño del producto puede crear su subasta");
        }
        if (!"subasta".equalsIgnoreCase(producto.tipoVenta())) {
            throw new SubastaInvalidaException("El producto no está publicado en modalidad subasta");
        }
        if (!"activo".equalsIgnoreCase(producto.estado())) {
            throw new SubastaInvalidaException("El producto no está activo");
        }
        if (subastas.existePorProducto(productoId)) {
            throw new SubastaInvalidaException("Este producto ya tiene una subasta");
        }

        Subasta nueva = Subasta.crear(productoId, usuarioId, precioInicial, incrementoMinimo,
                precioReserva, fechaInicio, fechaFin, OffsetDateTime.now());
        return subastas.guardar(nueva);
    }
}