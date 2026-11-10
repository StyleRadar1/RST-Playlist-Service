package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaylistTest {

    @Test
    void crearPlaylistValida() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Look Urbano Bogotá", "Hoodies y joggers", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);

        assertThat(p.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(p.getNombre()).isEqualTo("Look Urbano Bogotá");
        assertThat(p.getDescripcion()).isEqualTo("Hoodies y joggers");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PUBLICA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.URBANO);
        assertThat(p.isDestacada()).isFalse();
        assertThat(p.getTotalPrendas()).isZero();
        assertThat(p.getLikesCount()).isZero();
    }

    @Test
    void constructorPorDefecto() {
        Playlist p = new Playlist();
        assertThat(p.getId()).isNotNull();
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PUBLICA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.URBANO);
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void crearPlaylistUsuarioInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> Playlist.crear(null, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setNombreVacioOLargoLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setNombre(""))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> p.setNombre("a".repeat(101)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setDescripcionLargaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setDescripcion("a".repeat(301)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaHasta15Exitoso() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        for (int i = 1; i <= 15; i++) {
            p.agregarPrenda(new PrendaSnapshot(UUID.randomUUID(), "Prenda " + i, Instant.now()));
        }

        assertThat(p.getTotalPrendas()).isEqualTo(15);
        assertThatThrownBy(() -> p.agregarPrenda(new PrendaSnapshot(UUID.randomUUID(), "Prenda 16", Instant.now())))
                .isInstanceOf(LimitePrendasAlcanzadoException.class);
    }

    @Test
    void agregarPrendaNulaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        assertThatThrownBy(() -> p.agregarPrenda(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaDuplicadaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        UUID prendaId = UUID.randomUUID();
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Chaqueta", Instant.now()));

        assertThatThrownBy(() -> p.agregarPrenda(new PrendaSnapshot(prendaId, "Chaqueta Otra", Instant.now())))
                .isInstanceOf(PrendaDuplicadaException.class);
    }

    @Test
    void retirarPrendaPorPropietario() {
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        p.retirarPrenda(prendaId, usuarioId);
        assertThat(p.getTotalPrendas()).isZero();

        assertThatThrownBy(() -> p.retirarPrenda(prendaId, usuarioId))
                .isInstanceOf(PrendaNoEncontradaEnPlaylistException.class);
    }

    @Test
    void desvincularPrendaSilencioso() {
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        boolean removida = p.desvincularPrendaSilencioso(prendaId);
        assertThat(removida).isTrue();
        assertThat(p.getTotalPrendas()).isZero();

        boolean segunda = p.desvincularPrendaSilencioso(prendaId);
        assertThat(segunda).isFalse();
        assertThat(p.desvincularPrendaSilencioso(null)).isFalse();
    }

    @Test
    void alternarLikeYGuardado() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        UUID userLike = UUID.randomUUID();
        UUID userGuardado = UUID.randomUUID();

        p.alternarLike(userLike);
        assertThat(p.getLikesCount()).isEqualTo(1);
        p.alternarLike(userLike);
        assertThat(p.getLikesCount()).isZero();

        p.alternarGuardado(userGuardado);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
        p.alternarGuardado(userGuardado);
        assertThat(p.getGuardadosCount()).isZero();

        assertThatThrownBy(() -> p.alternarLike(null)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> p.alternarGuardado(null)).isInstanceOf(DomainException.class);
    }

    @Test
    void destacarYQuitarDestacado() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.destacar();
        assertThat(p.isDestacada()).isTrue();
        p.quitarDestacado();
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void validarAccesoLecturaPrivada() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Colección Privada", null, VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        p.validarAccesoLectura(usuarioId);
        assertThatThrownBy(() -> p.validarAccesoLectura(UUID.randomUUID()))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
    }

    @Test
    void validarAccesoLecturaPublica() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección Pública", null, VisibilidadPlaylist.PUBLICA, Estilo.FORMAL);
        p.validarAccesoLectura(null);
        p.validarAccesoLectura(UUID.randomUUID());
        assertThat(p.getNombre()).isEqualTo("Colección Pública");
    }

    @Test
    void editarLookbookExitoso() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.editar(usuarioId, "Modificado", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        assertThat(p.getNombre()).isEqualTo("Modificado");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
    }

    @Test
    void editarLookbookUsuarioAjenoLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        assertThatThrownBy(() -> p.editar(UUID.randomUUID(), "Modificado", null, null, null))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
    }

    @Test
    void retirarPrendaInvalidaLanzaExcepciones() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        assertThatThrownBy(() -> p.retirarPrenda(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
        assertThatThrownBy(() -> p.retirarPrenda(null, usuarioId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void constructorCompletoConValidaciones() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        UUID userLike = UUID.randomUUID();
        UUID userGuardado = UUID.randomUUID();

        Playlist p = new Playlist(
                id, usuarioId, "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.DEPORTIVO, true,
                Instant.now(), Instant.now(), List.of(new PrendaSnapshot(prendaId, "p", Instant.now())),
                Set.of(userLike), Set.of(userGuardado)
        );

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.isDestacada()).isTrue();
        assertThat(p.getTotalPrendas()).isEqualTo(1);
        assertThat(p.getLikesCount()).isEqualTo(1);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
    }

    @Test
    void settersYGetters() {
        Playlist p = new Playlist();
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        p.setId(id);
        p.setUsuarioId(usuarioId);
        p.setVisibilidad(VisibilidadPlaylist.PRIVADA);
        p.setEstilo(Estilo.FORMAL);
        p.setDestacada(true);
        p.setFechaActualizacion(Instant.now());

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
        assertThat(p.isDestacada()).isTrue();
    }
}
