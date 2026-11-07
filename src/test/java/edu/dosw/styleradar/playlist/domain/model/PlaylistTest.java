package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaylistTest {

    @Test
    void crearPlaylistValida() {
        Playlist p = Playlist.crear(10L, "Look Urbano Bogotá", "Hoodies y joggers", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);

        assertThat(p.getUsuarioId()).isEqualTo(10L);
        assertThat(p.getNombre()).isEqualTo("Look Urbano Bogotá");
        assertThat(p.getDescripcion()).isEqualTo("Hoodies y joggers");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PUBLICA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.URBANO);
        assertThat(p.isDestacada()).isFalse();
        assertThat(p.getTotalPrendas()).isZero();
        assertThat(p.getLikesCount()).isZero();
    }

    @Test
    void crearPlaylistUsuarioInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> Playlist.crear(null, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setNombreVacioOLargoLanzaExcepcion() {
        Playlist p = Playlist.crear(10L, "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setNombre(""))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> p.setNombre("a".repeat(101)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setDescripcionLargaLanzaExcepcion() {
        Playlist p = Playlist.crear(10L, "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setDescripcion("a".repeat(301)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaHasta15Exitoso() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        for (long i = 1; i <= 15; i++) {
            p.agregarPrenda(new PrendaSnapshot(i, "Prenda " + i, Instant.now()));
        }

        assertThat(p.getTotalPrendas()).isEqualTo(15);
        assertThatThrownBy(() -> p.agregarPrenda(new PrendaSnapshot(16L, "Prenda 16", Instant.now())))
                .isInstanceOf(LimitePrendasAlcanzadoException.class);
    }

    @Test
    void agregarPrendaDuplicadaLanzaExcepcion() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(1L, "Chaqueta", Instant.now()));

        assertThatThrownBy(() -> p.agregarPrenda(new PrendaSnapshot(1L, "Chaqueta Otra", Instant.now())))
                .isInstanceOf(PrendaDuplicadaException.class);
    }

    @Test
    void retirarPrendaPorPropietario() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(50L, "Pantalón", Instant.now()));

        p.retirarPrenda(50L, 10L);
        assertThat(p.getTotalPrendas()).isZero();

        assertThatThrownBy(() -> p.retirarPrenda(50L, 10L))
                .isInstanceOf(PrendaNoEncontradaEnPlaylistException.class);
    }

    @Test
    void desvincularPrendaSilencioso() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(50L, "Pantalón", Instant.now()));

        boolean removida = p.desvincularPrendaSilencioso(50L);
        assertThat(removida).isTrue();
        assertThat(p.getTotalPrendas()).isZero();

        boolean segunda = p.desvincularPrendaSilencioso(50L);
        assertThat(segunda).isFalse();
    }

    @Test
    void alternarLikeYGuardado() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        p.alternarLike(100L);
        assertThat(p.getLikesCount()).isEqualTo(1);
        p.alternarLike(100L);
        assertThat(p.getLikesCount()).isZero();

        p.alternarGuardado(200L);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
        p.alternarGuardado(200L);
        assertThat(p.getGuardadosCount()).isZero();
    }

    @Test
    void destacarYQuitarDestacado() {
        Playlist p = Playlist.crear(10L, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.destacar();
        assertThat(p.isDestacada()).isTrue();
        p.quitarDestacado();
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void validarAccesoLecturaPrivada() {
        Playlist p = Playlist.crear(10L, "Colección Privada", null, VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        p.validarAccesoLectura(10L); // Creador puede
        assertThatThrownBy(() -> p.validarAccesoLectura(99L)) // Otro usuario falla
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
    }

    @Test
    void constructorCompletoConValidaciones() {
        Playlist p = new Playlist(
                1L, 10L, "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.DEPORTIVO, true,
                Instant.now(), Instant.now(), List.of(new PrendaSnapshot(1L, "p", Instant.now())),
                Set.of(100L), Set.of(200L)
        );

        assertThat(p.getId()).isEqualTo(1L);
        assertThat(p.isDestacada()).isTrue();
        assertThat(p.getTotalPrendas()).isEqualTo(1);
        assertThat(p.getLikesCount()).isEqualTo(1);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
    }
}
