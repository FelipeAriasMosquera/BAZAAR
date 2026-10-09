package com.bazaar.catalogo.mapper;

import com.bazaar.catalogo.dto.ProductoImagenResponse;
import com.bazaar.catalogo.dto.ProductoResponse;
import com.bazaar.catalogo.entity.Producto;
import com.bazaar.catalogo.entity.ProductoImagen;

import java.util.List;

public final class ProductoMapper {

    private ProductoMapper() {}

    public static ProductoResponse toResponse(Producto producto) {
        List<ProductoImagenResponse> imagenes = producto.getImagenes().stream()
                .map(ProductoMapper::toImagenResponse)
                .toList();

        return new ProductoResponse(
                producto.getId(),
                producto.getVendedorId(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getNombre(),
                producto.getSlug(),
                producto.getDescripcion(),
                producto.getTipoVenta().getValor(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getEstado().getValor(),
                producto.getFechaCreacion(),
                imagenes
        );
    }

    public static ProductoImagenResponse toImagenResponse(ProductoImagen imagen) {
        return new ProductoImagenResponse(imagen.getId(), imagen.getUrl(), imagen.getOrden());
    }
}
