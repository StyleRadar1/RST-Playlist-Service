package edu.dosw.styleradar.playlist.application.dto;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearPlaylistRequest(
    @NotBlank(message = "El nombre de la playlist es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    String nombre,

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    String descripcion,

    @NotNull(message = "La visibilidad es obligatoria")
    VisibilidadPlaylist visibilidad,

    Estilo estilo
) {}
