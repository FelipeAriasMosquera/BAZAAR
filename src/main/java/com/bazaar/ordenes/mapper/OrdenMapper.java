package com.bazaar.ordenes.mapper;

import com.bazaar.ordenes.dto.OrdenItemResponse;
import com.bazaar.ordenes.dto.OrdenResponse;
import com.bazaar.ordenes.entity.Orden;
import com.bazaar.ordenes.entity.OrdenItem;

import java.util.List;

public class OrdenMapper {

    private OrdenMapper() {}

    public static OrdenResponse toResponse(Orden orden) {
        List<OrdenItemResponse> items = orden.getItems().stream()
                .map(OrdenMapper::toItemResponse)
                .toList();

        return new OrdenResponse(
                orden.getId(),
                orden.getUsuario().getId(),
                orden.getDireccionId(),
                orden.getEstado().enBd(),
                orden.getSubtotal(),
                orden.getEnvio(),
                orden.getTotal(),
                orden.getFecha(),
                items
        );
    }

    public static OrdenItemResponse toItemResponse(OrdenItem item) {
        return new OrdenItemResponse(
                item.getId(),
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getSubastaId(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.subtotal()
        );
    }
}
