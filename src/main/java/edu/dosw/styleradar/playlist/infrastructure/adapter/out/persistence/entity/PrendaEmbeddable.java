package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class PrendaEmbeddable {

    @Column(name = "prenda_id", nullable = false)
    private Long prendaId;

    @Column(name = "nombre_prenda", length = 150)
    private String nombrePrenda;

    @Column(name = "fecha_asociacion")
    private Instant fechaAsociacion;

    public PrendaEmbeddable() {}

    public PrendaEmbeddable(Long prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this.prendaId = prendaId;
        this.nombrePrenda = nombrePrenda;
        this.fechaAsociacion = fechaAsociacion;
    }

    public Long getPrendaId() { return prendaId; }
    public void setPrendaId(Long prendaId) { this.prendaId = prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public void setNombrePrenda(String nombrePrenda) { this.nombrePrenda = nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(Instant fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }
}
