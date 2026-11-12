package edu.dosw.styleradar.playlist.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PrendaSnapshot {
    private UUID prendaId;
    private String nombrePrenda;
    private Instant fechaAsociacion;
    private boolean disponible;

    public PrendaSnapshot() {
        this.fechaAsociacion = Instant.now();
        this.disponible = true;
    }

    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true);
    }

    @Default
    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        if (prendaId == null) {
            throw new IllegalArgumentException("El ID de la prenda no puede ser nulo");
        }
        this.prendaId = prendaId;
        this.nombrePrenda = (nombrePrenda != null && !nombrePrenda.isBlank()) ? nombrePrenda.trim() : "Prenda #" + prendaId;
        this.fechaAsociacion = (fechaAsociacion != null) ? fechaAsociacion : Instant.now();
        this.disponible = disponible;
    }

    public void marcarNoDisponible() {
        this.disponible = false;
    }

    public void marcarDisponible() {
        this.disponible = true;
    }

    public UUID getPrendaId() { return prendaId; }
    public void setPrendaId(UUID prendaId) { this.prendaId = prendaId; }

    public String getNombrePrenda() { return nombrePrenda; }
    public void setNombrePrenda(String nombrePrenda) { this.nombrePrenda = nombrePrenda; }

    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(Instant fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

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
