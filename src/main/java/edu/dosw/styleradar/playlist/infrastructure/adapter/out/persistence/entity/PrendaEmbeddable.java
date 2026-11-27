package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
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

    @Column(name = "imagen_url", length = 512)
    private String imagenUrl;

    @Column(name = "precio", precision = 12, scale = 2)
    private BigDecimal precio;

    public PrendaEmbeddable() {}

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible,
                            String imagenUrl, BigDecimal precio) {
        this.prendaId = prendaId;
        this.nombrePrenda = nombrePrenda;
        this.fechaAsociacion = fechaAsociacion;
        this.disponible = disponible;
        this.imagenUrl = imagenUrl;
        this.precio = precio;
    }

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        this(prendaId, nombrePrenda, fechaAsociacion, disponible, null, null);
    }

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true, null, null);
    }

    public UUID getPrendaId() { return prendaId; }
    public void setPrendaId(UUID prendaId) { this.prendaId = prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public void setNombrePrenda(String nombrePrenda) { this.nombrePrenda = nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(Instant fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
