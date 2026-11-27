package edu.dosw.styleradar.playlist.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PrendaResponse(
    UUID prendaId,
    String nombrePrenda,
    String imagenUrl,
    BigDecimal precio,
    Instant fechaAsociacion,
    boolean disponible
) {}
