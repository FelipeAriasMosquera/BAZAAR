package com.bazaar.carrito.mapper;

import com.bazaar.carrito.dto.CarritoItemResponse;
import com.bazaar.carrito.dto.CarritoResponse;
import com.bazaar.carrito.entity.Carrito;
import com.bazaar.carrito.entity.CarritoItem;

import java.math.BigDecimal;
import java.util.List;

public class CarritoMapper {

    private CarritoMapper() {}

    public static CarritoResponse toResponse(Carrito carrito) {
        List<CarritoItemResponse> items = carrito.getItems().stream()
                .map(CarritoMapper::toItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CarritoItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(carrito.getId(), carrito.getUsuarioId(), items, total);
    }

    public static CarritoItemResponse toItemResponse(CarritoItem item) {
        BigDecimal precioUnitario = item.getProducto().getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));

        return new CarritoItemResponse(
                item.getId(),
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getProducto().getSlug(),
                precioUnitario,
                item.getCantidad(),
                subtotal
        );
    }
}
