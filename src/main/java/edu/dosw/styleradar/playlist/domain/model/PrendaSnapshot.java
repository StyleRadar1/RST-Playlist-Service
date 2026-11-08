package edu.dosw.styleradar.playlist.domain.model;

import java.time.Instant;
import java.util.Objects;

public class PrendaSnapshot {
    private final Long prendaId;
    private final String nombrePrenda;
    private final Instant fechaAsociacion;

    public PrendaSnapshot(Long prendaId, String nombrePrenda, Instant fechaAsociacion) {
        if (prendaId == null || prendaId <= 0) {
            throw new IllegalArgumentException("El ID de la prenda debe ser un número positivo");
        }
        this.prendaId = prendaId;
        this.nombrePrenda = (nombrePrenda != null && !nombrePrenda.isBlank()) ? nombrePrenda.trim() : "Prenda #" + prendaId;
        this.fechaAsociacion = (fechaAsociacion != null) ? fechaAsociacion : Instant.now();
    }

    public Long getPrendaId() { return prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrendaSnapshot that = (PrendaSnapshot) o;
        return Objects.equals(prendaId, that.prendaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prendaId);
    }
}
