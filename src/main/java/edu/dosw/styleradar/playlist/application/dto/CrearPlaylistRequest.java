package edu.dosw.styleradar.playlist.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CrearPlaylistRequest(
    @NotBlank(message = "El nombre de la playlist es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    String nombre,

    String descripcion,

    @NotNull(message = "La visibilidad es obligatoria")
    VisibilidadPlaylist visibilidad,

    Estilo estilo,

    @Schema(description = "IDs de las prendas aprobadas con las que se crea la playlist (mínimo 1)",
            example = "[\"018e3a2b-1111-7000-8000-000000000001\"]")
    @JsonAlias({"prendaIds", "prendas"})
    List<UUID> prendasIds
) {
    public CrearPlaylistRequest(String nombre, String descripcion, VisibilidadPlaylist visibilidad, Estilo estilo) {
        this(nombre, descripcion, visibilidad, estilo, null);
    }
}
