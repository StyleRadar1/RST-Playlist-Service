package edu.dosw.styleradar.playlist.application.dto;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;

import java.time.Instant;
import java.util.List;

public record PlaylistResponse(
    Long id,
    Long usuarioId,
    String nombre,
    String descripcion,
    VisibilidadPlaylist visibilidad,
    Estilo estilo,
    boolean destacada,
    Instant fechaCreacion,
    List<PrendaResponse> prendas,
    int likes,
    int guardados,
    int totalPrendas
) {}
