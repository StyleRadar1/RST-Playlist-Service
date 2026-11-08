package edu.dosw.styleradar.playlist.application.port.out;

public interface PlaylistEventPublisherPort {
    void publicarPlaylistDestacadaActualizada(Long playlistId, boolean destacada);
    void publicarPrendaAsociada(Long playlistId, Long prendaId);
}
