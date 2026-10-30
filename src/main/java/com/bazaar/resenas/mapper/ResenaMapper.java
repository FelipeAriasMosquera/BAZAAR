package com.bazaar.resenas.mapper;

import com.bazaar.resenas.dto.ResenaResponse;
import com.bazaar.resenas.entity.Resena;

public class ResenaMapper {

    private ResenaMapper() {}

    public static ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(
                resena.getId(),
                resena.getProducto().getId(),
                resena.getUsuario().getId(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getFecha()
        );
    }
}
