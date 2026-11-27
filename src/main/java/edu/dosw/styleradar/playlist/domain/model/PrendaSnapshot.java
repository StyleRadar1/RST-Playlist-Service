package edu.dosw.styleradar.playlist.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PrendaSnapshot {
    private final UUID prendaId;
    private final String nombrePrenda;
    private final Instant fechaAsociacion;
    private boolean disponible;
    private String imagenUrl;
    private BigDecimal precio;

    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true, null, null);
    }

    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        this(prendaId, nombrePrenda, fechaAsociacion, disponible, null, null);
    }

    @Default
    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible,
                          String imagenUrl, BigDecimal precio) {
        if (prendaId == null) {
            throw new IllegalArgumentException("El ID de la prenda no puede ser nulo");
        }
        this.prendaId = prendaId;
        this.nombrePrenda = (nombrePrenda != null && !nombrePrenda.isBlank()) ? nombrePrenda.trim() : "Prenda #" + prendaId;
        this.fechaAsociacion = (fechaAsociacion != null) ? fechaAsociacion : Instant.now();
        this.disponible = disponible;
        this.imagenUrl = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : null;
        this.precio = precio;
    }

    public void marcarNoDisponible() {
        this.disponible = false;
    }

    public void marcarDisponible() {
        this.disponible = true;
    }

    public boolean necesitaEnriquecimiento() {
        return (this.imagenUrl == null || this.imagenUrl.isBlank()) || this.precio == null;
    }

    public void completarDatosCatalogo(String imagenUrl, BigDecimal precio) {
        if ((this.imagenUrl == null || this.imagenUrl.isBlank()) && imagenUrl != null && !imagenUrl.isBlank()) {
            this.imagenUrl = imagenUrl.trim();
        }
        if (this.precio == null && precio != null) {
            this.precio = precio;
        }
    }

    public UUID getPrendaId() { return prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : null;
    }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

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
