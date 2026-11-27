package edu.dosw.styleradar.playlist.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record PrendaCatalogoInfo(UUID id, String nombre, boolean aprobada, String imagenUrl, BigDecimal precio) {

    public PrendaCatalogoInfo(UUID id, String nombre, boolean aprobada) {
        this(id, nombre, aprobada, null, null);
    }
}
