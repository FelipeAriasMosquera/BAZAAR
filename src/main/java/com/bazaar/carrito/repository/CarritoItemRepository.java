package com.bazaar.carrito.repository;

import com.bazaar.carrito.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    Optional<CarritoItem> findByCarrito_IdAndProducto_Id(Long carritoId, Long productoId);

    Optional<CarritoItem> findByIdAndCarrito_Id(Long id, Long carritoId);
}
