package edu.dosw.styleradar.playlist.application.dto;

import java.time.Instant;

public record PrendaResponse(
    Long prendaId,
    String nombrePrenda,
    Instant fechaAsociacion
) {}
