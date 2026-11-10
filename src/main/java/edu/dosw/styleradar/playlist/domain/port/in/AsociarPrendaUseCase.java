package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface AsociarPrendaUseCase {
    PlaylistResponse agregarPrenda(UUID usuarioId, UUID playlistId, AgregarPrendaRequest request);
    PlaylistResponse retirarPrenda(UUID usuarioId, UUID playlistId, UUID prendaId);
    void desvincularPrendaDeTodas(UUID prendaId);
}
