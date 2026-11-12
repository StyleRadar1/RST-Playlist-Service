package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.UUID;

@Embeddable
public class PrendaEmbeddable {

    @Column(name = "prenda_id", nullable = false)
    private UUID prendaId;

    @Column(name = "nombre_prenda", length = 150)
    private String nombrePrenda;

    @Column(name = "fecha_asociacion")
    private Instant fechaAsociacion;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;

    public PrendaEmbeddable() {}

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        this.prendaId = prendaId;
        this.nombrePrenda = nombrePrenda;
        this.fechaAsociacion = fechaAsociacion;
        this.disponible = disponible;
    }

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true);
    }

    public UUID getPrendaId() { return prendaId; }
    public void setPrendaId(UUID prendaId) { this.prendaId = prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public void setNombrePrenda(String nombrePrenda) { this.nombrePrenda = nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(Instant fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
