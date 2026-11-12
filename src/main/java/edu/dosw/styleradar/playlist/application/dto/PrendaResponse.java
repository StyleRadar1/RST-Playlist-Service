package edu.dosw.styleradar.playlist.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PrendaResponse(
    UUID prendaId,
    String nombrePrenda,
    Instant fechaAsociacion,
    boolean disponible
) {}
