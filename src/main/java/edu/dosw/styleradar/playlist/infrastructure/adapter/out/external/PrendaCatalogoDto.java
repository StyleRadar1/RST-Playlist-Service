package edu.dosw.styleradar.playlist.infrastructure.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrendaCatalogoDto(
        UUID id,
        @JsonAlias({"nombrePrenda", "titulo", "name"}) String nombre,
        @JsonAlias({"estadoPrenda", "status"}) String estado,
        Boolean aprobada,
        List<String> fotografias,
        @JsonAlias({"imagen", "imageUrl"}) String imagenUrl,
        BigDecimal precio) {

    public PrendaCatalogoDto(UUID id, String nombre, String estado, Boolean aprobada) {
        this(id, nombre, estado, aprobada, null, null, null);
    }

    public boolean estaAprobada() {
        if (estado != null && !estado.isBlank()) {
            return "APROBADA".equalsIgnoreCase(estado.trim());
        }
        return Boolean.TRUE.equals(aprobada);
    }

    public String primeraImagen() {
        if (fotografias != null) {
            for (String foto : fotografias) {
                if (foto != null && !foto.isBlank()) {
                    return foto.trim();
                }
            }
        }
        if (imagenUrl != null && !imagenUrl.isBlank()) {
            return imagenUrl.trim();
        }
        return null;
    }
}
