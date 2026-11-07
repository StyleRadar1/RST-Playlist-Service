package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.application.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistApplicationServiceTest {

    @Mock
    private PlaylistRepositoryPort repository;

    @Mock
    private PlaylistEventPublisherPort eventPublisher;

    private PlaylistApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PlaylistApplicationService(repository, eventPublisher);
    }

    @Test
    void crearPlaylistGuardaYRetornaResponse() {
        CrearPlaylistRequest request = new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        Playlist creada = Playlist.crear(10L, request.nombre(), request.descripcion(), request.visibilidad(), request.estilo());
        creada.setId(1L);

        when(repository.save(any(Playlist.class))).thenReturn(creada);

        PlaylistResponse response = service.crear(10L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Urbano");
    }

    @Test
    void obtenerPorIdExitoso() {
        Playlist p = Playlist.crear(10L, "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(p));

        PlaylistResponse response = service.obtenerPorId(5L, 10L);

        assertThat(response.id()).isEqualTo(5L);
    }

    @Test
    void obtenerPorIdNoExisteLanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPublicasYPorUsuario() {
        Playlist p = Playlist.crear(10L, "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(1L);
        when(repository.findAllPublicas()).thenReturn(List.of(p));
        when(repository.findByUsuarioId(10L)).thenReturn(List.of(p));
        when(repository.findByGuardadosUsuarioId(10L)).thenReturn(List.of(p));
        when(repository.findDestacadas()).thenReturn(List.of(p));

        assertThat(service.obtenerPublicas()).hasSize(1);
        assertThat(service.obtenerPorUsuario(10L)).hasSize(1);
        assertThat(service.obtenerGuardadas(10L)).hasSize(1);
        assertThat(service.obtenerDestacadas()).hasSize(1);
    }

    @Test
    void obtenerPopularesPaginadas() {
        Playlist p = Playlist.crear(10L, "Popular", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        when(repository.findPopulares(0, 10)).thenReturn(List.of(p));
        when(repository.countPublicas()).thenReturn(1L);

        PageResponse<PlaylistResponse> page = service.obtenerPopulares(0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1L);
    }

    @Test
    void editarPlaylistExitoso() {
        Playlist p = Playlist.crear(10L, "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = service.editar(10L, 1L, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL));

        assertThat(res.nombre()).isEqualTo("Nuevo");
    }

    @Test
    void destacarValidaMaximo3Playlists() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(3L);

        assertThatThrownBy(() -> service.destacar(10L, 1L))
                .isInstanceOf(LimiteDestacadasSuperadoException.class);
    }

    @Test
    void destacarExitosoPublicaEvento() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(2L);
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = service.destacar(10L, 1L);

        assertThat(res.destacada()).isTrue();
        verify(eventPublisher).publicarPlaylistDestacadaActualizada(1L, true);

        // Si ya es destacada, no vuelve a guardar
        PlaylistResponse res2 = service.destacar(10L, 1L);
        assertThat(res2.destacada()).isTrue();
    }

    @Test
    void quitarDestacadoExitoso() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        p.destacar();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = service.quitarDestacado(10L, 1L);

        assertThat(res.destacada()).isFalse();
        verify(eventPublisher).publicarPlaylistDestacadaActualizada(1L, false);

        // Si ya no es destacada, no vuelve a guardar
        PlaylistResponse res2 = service.quitarDestacado(10L, 1L);
        assertThat(res2.destacada()).isFalse();
    }

    @Test
    void agregarPrendaExitoso() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = service.agregarPrenda(10L, 1L, new AgregarPrendaRequest(50L, "Gorra"));

        assertThat(res.totalPrendas()).isEqualTo(1);
        verify(eventPublisher).publicarPrendaAsociada(1L, 50L);
    }

    @Test
    void retirarPrendaExitoso() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        p.agregarPrenda(new PrendaSnapshot(50L, "Gorra", Instant.now()));
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = service.retirarPrenda(10L, 1L, 50L);

        assertThat(res.totalPrendas()).isZero();
    }

    @Test
    void alternarLikeYGuardado() {
        Playlist p = Playlist.crear(10L, "Nombre", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        service.alternarLike(10L, 1L);
        service.alternarGuardado(10L, 1L);

        verify(repository, times(2)).save(p);
    }

    @Test
    void eliminarExitoso() {
        Playlist p = Playlist.crear(10L, "A Borrar", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(p));

        service.eliminar(10L, 1L);

        verify(repository).delete(1L);
    }

    @Test
    void desvincularPrendaDeTodas() {
        Playlist p1 = Playlist.crear(10L, "Look 1", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p1.agregarPrenda(new PrendaSnapshot(99L, "Gorra", Instant.now()));

        when(repository.findAllConPrenda(99L)).thenReturn(List.of(p1));

        service.desvincularPrendaDeTodas(99L);

        assertThat(p1.getTotalPrendas()).isZero();
        verify(repository).save(p1);
    }
}
