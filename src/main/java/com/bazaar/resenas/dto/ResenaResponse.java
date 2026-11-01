package com.bazaar.resenas.dto;

import java.time.Instant;

// TODO: agregar el nombre del autor cuando se decida qué campo de Usuario se puede mostrar
// (no se expone el email por privacidad).
public record ResenaResponse(
        Long id,
        Long productoId,
        Long usuarioId,
        int calificacion,
        String comentario,
        Instant fecha
) {}
