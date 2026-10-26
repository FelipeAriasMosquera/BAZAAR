package com.bazaar.subastas.puertos;

import java.util.Optional;

public interface ProductoPort {

    record ProductoInfo(Long vendedorId, String tipoVenta, String estado) {}

    Optional<ProductoInfo> buscar(Long productoId);
}