package edu.dosw.styleradar.playlist.domain.port.out;

import java.util.UUID;

public interface PlaylistEventPublisherPort {
    void publicarPlaylistDestacadaActualizada(UUID playlistId, boolean destacada);
    void publicarPrendaAsociada(UUID playlistId, UUID prendaId);
}
