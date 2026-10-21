package com.bazaar.ordenes.dto;

import jakarta.validation.constraints.NotNull;

/** Sirve tanto para crear la orden (checkout) como para asignarle dirección después. */
public record DireccionOrdenRequest(

        @NotNull(message = "La dirección de envío es obligatoria")
        Long direccionId
) {}
