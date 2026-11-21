package edu.dosw.styleradar.playlist.infrastructure.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrendaCatalogoDto(
        UUID id,
        @JsonAlias({"nombrePrenda", "titulo", "name"}) String nombre,
        @JsonAlias({"estadoPrenda", "status"}) String estado,
        Boolean aprobada) {

    public boolean estaAprobada() {
        if (estado != null && !estado.isBlank()) {
            return "APROBADA".equalsIgnoreCase(estado.trim());
        }
        return Boolean.TRUE.equals(aprobada);
    }
}
