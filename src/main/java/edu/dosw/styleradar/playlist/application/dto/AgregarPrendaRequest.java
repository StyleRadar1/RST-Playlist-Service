package edu.dosw.styleradar.playlist.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AgregarPrendaRequest(
    @NotNull(message = "El ID de la prenda es obligatorio")
    UUID prendaId,
    String nombrePrenda
) {}
