package edu.dosw.styleradar.playlist.application.dto;

import jakarta.validation.constraints.NotNull;

public record AgregarPrendaRequest(
    @NotNull(message = "El ID de la prenda es obligatorio")
    Long prendaId,
    String nombrePrenda
) {}
