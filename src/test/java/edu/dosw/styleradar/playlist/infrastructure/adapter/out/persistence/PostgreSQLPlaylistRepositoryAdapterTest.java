package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPlaylistRepositoryAdapterTest {

    @Mock
    private SpringDataPlaylistRepository springDataRepository;

    private PostgreSQLPlaylistRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgreSQLPlaylistRepositoryAdapter(springDataRepository);
    }

    @Test
    void guardarYRecuperarPlaylist() {
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(1L);
        entity.setUsuarioId(10L);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.save(any())).thenReturn(entity);
        when(springDataRepository.findById(1L)).thenReturn(Optional.of(entity));

        Playlist p = Playlist.crear(10L, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        Playlist guardada = adapter.save(p);
        Optional<Playlist> opt = adapter.findById(1L);

        assertThat(guardada.getId()).isEqualTo(1L);
        assertThat(opt).isPresent();
    }

    @Test
    void consultasYConteo() {
        when(springDataRepository.countByDestacadaTrue()).thenReturn(2L);
        when(springDataRepository.findAllByPrendaId(5L)).thenReturn(List.of());

        assertThat(adapter.countDestacadas()).isEqualTo(2L);
        assertThat(adapter.findAllConPrenda(5L)).isEmpty();
        adapter.delete(1L);
        verify(springDataRepository).deleteById(1L);
    }
}
