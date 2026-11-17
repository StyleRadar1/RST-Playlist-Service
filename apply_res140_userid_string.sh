#!/usr/bin/env bash
set -e

echo "=========================================================================="
echo "  RES-140 (H20): MIGRACIÓN DE usuarioId A String Y VALIDACIÓN 401         "
echo "=========================================================================="

# 1. Excepción NoAutorizadoException para cabecera X-User-Id ausente o vacía (401)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/exception/NoAutorizadoException.java
package edu.dosw.styleradar.playlist.domain.exception;

public class NoAutorizadoException extends DomainException {
    public NoAutorizadoException(String message) {
        super(message);
    }
}
JAVA

# 2. Actualizar Dominio: Playlist (id=UUID, prendaId=UUID, usuarioId=String)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/model/Playlist.java
package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;

import java.time.Instant;
import java.util.*;

public class Playlist {
    public static final int MAX_PRENDAS = 15; // RNF-12
    public static final int MAX_DESTACADAS = 3; // SR-RF-72

    private UUID id;
    private String usuarioId;
    private String nombre;
    private String descripcion;
    private VisibilidadPlaylist visibilidad;
    private Estilo estilo;
    private boolean destacada;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    private final List<PrendaSnapshot> prendas = new ArrayList<>();
    private final Set<String> likesUsuarios = new HashSet<>();
    private final Set<String> guardadosUsuarios = new HashSet<>();

    public Playlist() {
        this.id = UUID.randomUUID();
        this.visibilidad = VisibilidadPlaylist.PUBLICA;
        this.estilo = Estilo.URBANO;
        this.destacada = false;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public Playlist(UUID id, String usuarioId, String nombre, String descripcion,
                    VisibilidadPlaylist visibilidad, Estilo estilo, boolean destacada,
                    Instant fechaCreacion, Instant fechaActualizacion,
                    List<PrendaSnapshot> prendas, Set<String> likesUsuarios, Set<String> guardadosUsuarios) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.usuarioId = usuarioId;
        asignarNombre(nombre);
        asignarDescripcion(descripcion);
        this.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        this.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        this.destacada = destacada;
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : Instant.now();
        this.fechaActualizacion = (fechaActualizacion != null) ? fechaActualizacion : this.fechaCreacion;
        asignarPrendas(prendas);
        asignarLikesUsuarios(likesUsuarios);
        asignarGuardadosUsuarios(guardadosUsuarios);
    }

    public static Playlist crear(String usuarioId, String nombre, String descripcion,
                                 VisibilidadPlaylist visibilidad, Estilo estilo) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new NoAutorizadoException("El ID del usuario creador es obligatorio");
        }
        Playlist p = new Playlist();
        p.id = UUID.randomUUID();
        p.usuarioId = usuarioId.trim();
        p.asignarNombre(nombre);
        p.asignarDescripcion(descripcion);
        p.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        p.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        p.destacada = false;
        p.fechaCreacion = Instant.now();
        p.fechaActualizacion = p.fechaCreacion;
        return p;
    }

    public void agregarPrenda(PrendaSnapshot prenda) {
        if (prenda == null) {
            throw new DomainException("La prenda no puede ser nula");
        }
        if (this.prendas.size() >= MAX_PRENDAS) {
            throw new LimitePrendasAlcanzadoException("Límite máximo de " + MAX_PRENDAS + " prendas alcanzado");
        }
        boolean yaExiste = this.prendas.stream().anyMatch(p -> p.getPrendaId().equals(prenda.getPrendaId()));
        if (yaExiste) {
            throw new PrendaDuplicadaException("La prenda ya se encuentra asociada a esta playlist");
        }
        this.prendas.add(prenda);
        this.fechaActualizacion = Instant.now();
    }

    public void retirarPrenda(UUID prendaId, String usuarioEjecutorId) {
        validarPropietario(usuarioEjecutorId);
        if (prendaId == null) {
            throw new DomainException("El ID de prenda es obligatorio");
        }
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (!removida) {
            throw new PrendaNoEncontradaEnPlaylistException("La prenda no pertenece a la playlist");
        }
        this.fechaActualizacion = Instant.now();
    }

    public boolean desvincularPrendaSilencioso(UUID prendaId) {
        if (prendaId == null) return false;
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (removida) {
            this.fechaActualizacion = Instant.now();
        }
        return removida;
    }

    public boolean marcarPrendaNoDisponible(UUID prendaId) {
        if (prendaId == null) return false;
        boolean encontrada = false;
        for (PrendaSnapshot p : this.prendas) {
            if (p.getPrendaId().equals(prendaId)) {
                p.marcarNoDisponible();
                encontrada = true;
            }
        }
        if (encontrada) {
            this.fechaActualizacion = Instant.now();
        }
        return encontrada;
    }

    public void destacar() {
        this.destacada = true;
        this.fechaActualizacion = Instant.now();
    }

    public void quitarDestacado() {
        this.destacada = false;
        this.fechaActualizacion = Instant.now();
    }

    public void alternarLike(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new NoAutorizadoException("El ID de usuario es obligatorio");
        }
        String idLimpio = usuarioId.trim();
        if (this.likesUsuarios.contains(idLimpio)) {
            this.likesUsuarios.remove(idLimpio);
        } else {
            this.likesUsuarios.add(idLimpio);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void alternarGuardado(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new NoAutorizadoException("El ID de usuario es obligatorio");
        }
        String idLimpio = usuarioId.trim();
        if (this.guardadosUsuarios.contains(idLimpio)) {
            this.guardadosUsuarios.remove(idLimpio);
        } else {
            this.guardadosUsuarios.add(idLimpio);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void editar(String usuarioId, String nombre, String descripcion,
                       VisibilidadPlaylist visibilidad, Estilo estilo) {
        validarPropietario(usuarioId);
        asignarNombre(nombre);
        asignarDescripcion(descripcion);
        if (visibilidad != null) {
            this.visibilidad = visibilidad;
        }
        if (estilo != null) {
            this.estilo = estilo;
        }
        this.fechaActualizacion = Instant.now();
    }

    public void validarPropietario(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank() || !usuarioId.trim().equals(this.usuarioId)) {
            throw new AccesoDenegadoPlaylistException("No tienes permisos para modificar esta playlist");
        }
    }

    public void validarAccesoLectura(String usuarioConsultaId) {
        if (this.visibilidad == VisibilidadPlaylist.PRIVADA) {
            if (usuarioConsultaId == null || usuarioConsultaId.isBlank() || !usuarioConsultaId.trim().equals(this.usuarioId)) {
                throw new AccesoDenegadoPlaylistException("No tienes permisos para consultar esta playlist privada");
            }
        }
    }

    private void asignarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre de la playlist es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new DomainException("El nombre no puede superar los 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    private void asignarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 300) {
            throw new DomainException("La descripción no puede superar los 300 caracteres");
        }
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
    }

    private void asignarPrendas(List<PrendaSnapshot> prendas) {
        this.prendas.clear();
        if (prendas != null) {
            if (prendas.size() > MAX_PRENDAS) {
                throw new LimitePrendasAlcanzadoException("La playlist no admite más de " + MAX_PRENDAS + " prendas (RNF-12)");
            }
            this.prendas.addAll(prendas);
        }
    }

    private void asignarLikesUsuarios(Set<String> likes) {
        this.likesUsuarios.clear();
        if (likes != null) this.likesUsuarios.addAll(likes);
    }

    private void asignarGuardadosUsuarios(Set<String> guardados) {
        this.guardadosUsuarios.clear();
        if (guardados != null) this.guardadosUsuarios.addAll(guardados);
    }

    public void setNombre(String nombre) { asignarNombre(nombre); }
    public void setDescripcion(String descripcion) { asignarDescripcion(descripcion); }
    public void setPrendas(List<PrendaSnapshot> prendas) { asignarPrendas(prendas); }
    public void setLikesUsuarios(Set<String> likes) { asignarLikesUsuarios(likes); }
    public void setGuardadosUsuarios(Set<String> guardados) { asignarGuardadosUsuarios(guardados); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public VisibilidadPlaylist getVisibilidad() { return visibilidad; }
    public void setVisibilidad(VisibilidadPlaylist visibilidad) { this.visibilidad = visibilidad; }
    public Estilo getEstilo() { return estilo; }
    public void setEstilo(Estilo estilo) { this.estilo = estilo; }
    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<PrendaSnapshot> getPrendas() { return Collections.unmodifiableList(prendas); }
    public Set<String> getLikesUsuarios() { return Collections.unmodifiableSet(likesUsuarios); }
    public Set<String> getGuardadosUsuarios() { return Collections.unmodifiableSet(guardadosUsuarios); }

    public int getLikesCount() { return likesUsuarios.size(); }
    public int getGuardadosCount() { return guardadosUsuarios.size(); }
    public int getTotalPrendas() { return prendas.size(); }
}
JAVA

# 3. Actualizar Puertos In / Out a String para usuarioId
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/CrearPlaylistUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface CrearPlaylistUseCase {
    PlaylistResponse crear(String usuarioId, CrearPlaylistRequest request);
    PlaylistResponse editar(String usuarioId, UUID playlistId, EditarPlaylistRequest request);
    void eliminar(String usuarioId, UUID playlistId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/ConsultarPlaylistUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarPlaylistUseCase {
    PlaylistResponse obtenerPorId(UUID id, String usuarioConsultaId);
    List<PlaylistResponse> obtenerPublicas();
    PageResponse<PlaylistResponse> obtenerPopulares(int page, int size);
    List<PlaylistResponse> obtenerPorUsuario(String usuarioId);
    List<PlaylistResponse> obtenerGuardadas(String usuarioId);
    List<PlaylistResponse> obtenerDestacadas();
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/DestacarPlaylistUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface DestacarPlaylistUseCase {
    PlaylistResponse destacar(String usuarioId, UUID playlistId);
    PlaylistResponse quitarDestacado(String usuarioId, UUID playlistId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/AsociarPrendaUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface AsociarPrendaUseCase {
    PlaylistResponse agregarPrenda(String usuarioId, UUID playlistId, AgregarPrendaRequest request);
    PlaylistResponse retirarPrenda(String usuarioId, UUID playlistId, UUID prendaId);
    void desvincularPrendaDeTodas(UUID prendaId);
    void marcarPrendaNoDisponibleEnTodas(UUID prendaId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/InteractuarPlaylistUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface InteractuarPlaylistUseCase {
    PlaylistResponse alternarLike(String usuarioId, UUID playlistId);
    PlaylistResponse alternarGuardado(String usuarioId, UUID playlistId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/out/PlaylistRepositoryPort.java
package edu.dosw.styleradar.playlist.domain.port.out;

import edu.dosw.styleradar.playlist.domain.model.Playlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepositoryPort {
    Playlist save(Playlist playlist);
    Optional<Playlist> findById(UUID id);
    List<Playlist> findAllPublicas();
    List<Playlist> findPopulares(int page, int size);
    long countPublicas();
    List<Playlist> findByUsuarioId(String usuarioId);
    List<Playlist> findByGuardadosUsuarioId(String usuarioId);
    List<Playlist> findDestacadas();
    long countDestacadas();
    void delete(UUID id);
    List<Playlist> findAllConPrenda(UUID prendaId);
}
JAVA

# 4. Actualizar DTO PlaylistResponse (usuarioId=String, id=UUID)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/dto/PlaylistResponse.java
package edu.dosw.styleradar.playlist.application.dto;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
    UUID id,
    String usuarioId,
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
JAVA

# 5. Actualizar Implementaciones de Casos de Uso
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/usecase/CrearPlaylistUseCaseImpl.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.CrearPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CrearPlaylistUseCaseImpl implements CrearPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public CrearPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse crear(String usuarioId, CrearPlaylistRequest request) {
        Playlist playlist = Playlist.crear(
                usuarioId,
                request.nombre(),
                request.descripcion(),
                request.visibilidad(),
                request.estilo()
        );
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse editar(String usuarioId, UUID playlistId, EditarPlaylistRequest request) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.editar(usuarioId, request.nombre(), request.descripcion(), request.visibilidad(), request.estilo());
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public void eliminar(String usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.validarPropietario(usuarioId);
        repository.delete(playlistId);
    }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/usecase/ConsultarPlaylistUseCaseImpl.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.ConsultarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarPlaylistUseCaseImpl implements ConsultarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public ConsultarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse obtenerPorId(UUID id, String usuarioConsultaId) {
        Playlist playlist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + id));
        playlist.validarAccesoLectura(usuarioConsultaId);
        return mapper.toResponse(playlist);
    }

    @Override
    public List<PlaylistResponse> obtenerPublicas() {
        return repository.findAllPublicas().stream().map(mapper::toResponse).toList();
    }

    @Override
    public PageResponse<PlaylistResponse> obtenerPopulares(int page, int size) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size < 1 || size > 100) ? 20 : size;
        List<PlaylistResponse> content = repository.findPopulares(sanitizedPage, sanitizedSize)
                .stream().map(mapper::toResponse).toList();
        long total = repository.countPublicas();
        int totalPages = (int) Math.ceil((double) total / sanitizedSize);
        return new PageResponse<>(content, sanitizedPage, sanitizedSize, total, totalPages, sanitizedPage == 0, sanitizedPage >= totalPages - 1);
    }

    @Override
    public List<PlaylistResponse> obtenerPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerGuardadas(String usuarioId) {
        return repository.findByGuardadosUsuarioId(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerDestacadas() {
        return repository.findDestacadas().stream().map(mapper::toResponse).toList();
    }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/usecase/DestacarPlaylistUseCaseImpl.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.DestacarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DestacarPlaylistUseCaseImpl implements DestacarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistEventPublisherPort eventPublisher;
    private final PlaylistDtoMapper mapper;

    public DestacarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistEventPublisherPort eventPublisher, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse destacar(String usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        if (!playlist.isDestacada()) {
            if (repository.countDestacadas() >= Playlist.MAX_DESTACADAS) {
                throw new LimiteDestacadasSuperadoException("Límite máximo de " + Playlist.MAX_DESTACADAS + " playlists destacadas alcanzado (SR-RF-72)");
            }
            playlist.destacar();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), true);
            return mapper.toResponse(guardada);
        }
        return mapper.toResponse(playlist);
    }

    @Override
    public PlaylistResponse quitarDestacado(String usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        if (playlist.isDestacada()) {
            playlist.quitarDestacado();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), false);
            return mapper.toResponse(guardada);
        }
        return mapper.toResponse(playlist);
    }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/usecase/AsociarPrendaUseCaseImpl.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AsociarPrendaUseCaseImpl implements AsociarPrendaUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistEventPublisherPort eventPublisher;
    private final PlaylistDtoMapper mapper;

    public AsociarPrendaUseCaseImpl(PlaylistRepositoryPort repository, PlaylistEventPublisherPort eventPublisher, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse agregarPrenda(String usuarioId, UUID playlistId, AgregarPrendaRequest request) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.validarPropietario(usuarioId);
        PrendaSnapshot snapshot = new PrendaSnapshot(request.prendaId(), request.nombrePrenda(), Instant.now(), true);
        playlist.agregarPrenda(snapshot);
        Playlist guardada = repository.save(playlist);
        eventPublisher.publicarPrendaAsociada(guardada.getId(), request.prendaId());
        return mapper.toResponse(guardada);
    }

    @Override
    public PlaylistResponse retirarPrenda(String usuarioId, UUID playlistId, UUID prendaId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.retirarPrenda(prendaId, usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public void desvincularPrendaDeTodas(UUID prendaId) {
        List<Playlist> afectadas = repository.findAllConPrenda(prendaId);
        for (Playlist p : afectadas) {
            if (p.desvincularPrendaSilencioso(prendaId)) {
                repository.save(p);
            }
        }
    }

    @Override
    public void marcarPrendaNoDisponibleEnTodas(UUID prendaId) {
        List<Playlist> afectadas = repository.findAllConPrenda(prendaId);
        for (Playlist p : afectadas) {
            if (p.marcarPrendaNoDisponible(prendaId)) {
                repository.save(p);
            }
        }
    }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/usecase/InteractuarPlaylistUseCaseImpl.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.InteractuarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class InteractuarPlaylistUseCaseImpl implements InteractuarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public InteractuarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse alternarLike(String usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.alternarLike(usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse alternarGuardado(String usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.alternarGuardado(usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }
}
JAVA

# 6. Actualizar Persistencia JPA (PlaylistEntity, Repository, Adapter)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/entity/PlaylistEntity.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "playlists")
public class PlaylistEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilidadPlaylist visibilidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estilo estilo;

    @Column(nullable = false)
    private boolean destacada;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_prendas", joinColumns = @JoinColumn(name = "playlist_id"))
    private List<PrendaEmbeddable> prendas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_likes", joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "usuario_id", nullable = false, length = 100)
    private Set<String> likesUsuarios = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_guardados", joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "usuario_id", nullable = false, length = 100)
    private Set<String> guardadosUsuarios = new HashSet<>();

    public PlaylistEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public VisibilidadPlaylist getVisibilidad() { return visibilidad; }
    public void setVisibilidad(VisibilidadPlaylist visibilidad) { this.visibilidad = visibilidad; }
    public Estilo getEstilo() { return estilo; }
    public void setEstilo(Estilo estilo) { this.estilo = estilo; }
    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public List<PrendaEmbeddable> getPrendas() { return prendas; }
    public void setPrendas(List<PrendaEmbeddable> prendas) { this.prendas = prendas; }
    public Set<String> getLikesUsuarios() { return likesUsuarios; }
    public void setLikesUsuarios(Set<String> likesUsuarios) { this.likesUsuarios = likesUsuarios; }
    public Set<String> getGuardadosUsuarios() { return guardadosUsuarios; }
    public void setGuardadosUsuarios(Set<String> guardadosUsuarios) { this.guardadosUsuarios = guardadosUsuarios; }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/SpringDataPlaylistRepository.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataPlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {

    List<PlaylistEntity> findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist visibilidad);

    List<PlaylistEntity> findByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);

    List<PlaylistEntity> findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(String usuarioId);

    List<PlaylistEntity> findByDestacadaTrueOrderByFechaActualizacionDesc();

    long countByDestacadaTrue();

    long countByVisibilidad(VisibilidadPlaylist visibilidad);

    @Query("SELECT p FROM PlaylistEntity p WHERE p.visibilidad = 'PUBLICA' ORDER BY SIZE(p.likesUsuarios) DESC, p.fechaCreacion DESC")
    Page<PlaylistEntity> findPopulares(Pageable pageable);

    @Query("SELECT DISTINCT p FROM PlaylistEntity p JOIN p.prendas pr WHERE pr.prendaId = :prendaId")
    List<PlaylistEntity> findAllByPrendaId(@Param("prendaId") UUID prendaId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/PostgreSQLPlaylistRepositoryAdapter.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.mapper.PlaylistEntityMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PostgreSQLPlaylistRepositoryAdapter implements PlaylistRepositoryPort {

    private final SpringDataPlaylistRepository springDataRepository;
    private final PlaylistEntityMapper mapper;

    public PostgreSQLPlaylistRepositoryAdapter(SpringDataPlaylistRepository springDataRepository, PlaylistEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Playlist save(Playlist domain) {
        PlaylistEntity entity = mapper.toEntity(domain);
        PlaylistEntity guardada = springDataRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Playlist> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Playlist> findAllPublicas() {
        return springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findPopulares(int page, int size) {
        return springDataRepository.findPopulares(PageRequest.of(page, size))
                .getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countPublicas() {
        return springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA);
    }

    @Override
    public List<Playlist> findByUsuarioId(String usuarioId) {
        return springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findByGuardadosUsuarioId(String usuarioId) {
        return springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(usuarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findDestacadas() {
        return springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()
                .stream().limit(3).map(mapper::toDomain).toList();
    }

    @Override
    public long countDestacadas() {
        return springDataRepository.countByDestacadaTrue();
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<Playlist> findAllConPrenda(UUID prendaId) {
        return springDataRepository.findAllByPrendaId(prendaId)
                .stream().map(mapper::toDomain).toList();
    }
}
JAVA

# 7. Actualizar Controladores, OpenAPI, ExceptionHandler y Sonar fixes
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/rest/doc/PlaylistApi.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc;

import edu.dosw.styleradar.playlist.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Playlists", description = "Playlists y colecciones temáticas de prendas de vestir")
public interface PlaylistApi {

    @Operation(summary = "Crear nueva playlist de moda")
    @ApiResponse(responseCode = "201", description = "Playlist creada exitosamente")
    ResponseEntity<PlaylistResponse> crear(
            @Parameter(description = "ID de usuario propagado por el Gateway", example = "15") String usuarioId,
            CrearPlaylistRequest request);

    @Operation(summary = "Consultar playlist por ID")
    ResponseEntity<PlaylistResponse> obtenerPorId(
            UUID id,
            @Parameter(description = "ID de usuario opcional", example = "15") String usuarioId);

    @Operation(summary = "Consultar playlists públicas")
    ResponseEntity<List<PlaylistResponse>> obtenerPublicas();

    @Operation(summary = "Consultar las 3 playlists destacadas de Home (SR-RF-72)")
    ResponseEntity<List<PlaylistResponse>> obtenerDestacadas();

    @Operation(summary = "Consultar playlists populares paginadas")
    ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(int page, int size);

    @Operation(summary = "Editar playlist propia")
    ResponseEntity<PlaylistResponse> editar(String usuarioId, UUID id, EditarPlaylistRequest request);

    @Operation(summary = "Fijar playlist como destacada (máx 3 · SR-RF-72)")
    ResponseEntity<PlaylistResponse> destacar(String usuarioId, UUID id);

    @Operation(summary = "Quitar playlist de destacadas")
    ResponseEntity<PlaylistResponse> quitarDestacado(String usuarioId, UUID id);

    @Operation(summary = "Asociar prenda a la playlist (máx 15 · RNF-12)")
    ResponseEntity<PlaylistResponse> agregarPrenda(String usuarioId, UUID id, AgregarPrendaRequest request);

    @Operation(summary = "Retirar prenda de la playlist")
    ResponseEntity<PlaylistResponse> retirarPrenda(String usuarioId, UUID id, UUID prendaId);

    @Operation(summary = "Alternar Me Gusta en la playlist")
    ResponseEntity<PlaylistResponse> alternarLike(String usuarioId, UUID id);

    @Operation(summary = "Alternar guardado en perfil")
    ResponseEntity<PlaylistResponse> alternarGuardado(String usuarioId, UUID id);

    @Operation(summary = "Eliminar playlist")
    ResponseEntity<Void> eliminar(String usuarioId, UUID id);

    @Operation(summary = "Consultar playlists creadas por el usuario")
    ResponseEntity<List<PlaylistResponse>> misPlaylists(String usuarioId);

    @Operation(summary = "Consultar playlists guardadas por el usuario")
    ResponseEntity<List<PlaylistResponse>> misGuardadas(String usuarioId);
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/rest/PlaylistController.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc.PlaylistApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/playlists")
public class PlaylistController implements PlaylistApi {

    private final CrearPlaylistUseCase crearPlaylistUseCase;
    private final ConsultarPlaylistUseCase consultarPlaylistUseCase;
    private final DestacarPlaylistUseCase destacarPlaylistUseCase;
    private final AsociarPrendaUseCase asociarPrendaUseCase;
    private final InteractuarPlaylistUseCase interactuarPlaylistUseCase;

    public PlaylistController(CrearPlaylistUseCase crearPlaylistUseCase,
                              ConsultarPlaylistUseCase consultarPlaylistUseCase,
                              DestacarPlaylistUseCase destacarPlaylistUseCase,
                              AsociarPrendaUseCase asociarPrendaUseCase,
                              InteractuarPlaylistUseCase interactuarPlaylistUseCase) {
        this.crearPlaylistUseCase = crearPlaylistUseCase;
        this.consultarPlaylistUseCase = consultarPlaylistUseCase;
        this.destacarPlaylistUseCase = destacarPlaylistUseCase;
        this.asociarPrendaUseCase = asociarPrendaUseCase;
        this.interactuarPlaylistUseCase = interactuarPlaylistUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<PlaylistResponse> crear(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @Valid @RequestBody CrearPlaylistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(crearPlaylistUseCase.crear(validarUsuarioId(usuarioId), request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> obtenerPorId(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String usuarioId) {
        String usuarioNormalizado = (usuarioId == null || usuarioId.isBlank()) ? null : usuarioId.trim();
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorId(id, usuarioNormalizado));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> obtenerPublicas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPublicas());
    }

    @Override
    @GetMapping("/destacadas")
    public ResponseEntity<List<PlaylistResponse>> obtenerDestacadas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerDestacadas());
    }

    @Override
    @GetMapping("/populares")
    public ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPopulares(page, size));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> editar(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody EditarPlaylistRequest request) {
        return ResponseEntity.ok(crearPlaylistUseCase.editar(validarUsuarioId(usuarioId), id, request));
    }

    @Override
    @PutMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> destacar(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(destacarPlaylistUseCase.destacar(validarUsuarioId(usuarioId), id));
    }

    @Override
    @DeleteMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> quitarDestacado(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(destacarPlaylistUseCase.quitarDestacado(validarUsuarioId(usuarioId), id));
    }

    @Override
    @PostMapping("/{id}/prendas")
    public ResponseEntity<PlaylistResponse> agregarPrenda(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody AgregarPrendaRequest request) {
        return ResponseEntity.ok(asociarPrendaUseCase.agregarPrenda(validarUsuarioId(usuarioId), id, request));
    }

    @Override
    @DeleteMapping("/{id}/prendas/{prendaId}")
    public ResponseEntity<PlaylistResponse> retirarPrenda(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @PathVariable UUID prendaId) {
        return ResponseEntity.ok(asociarPrendaUseCase.retirarPrenda(validarUsuarioId(usuarioId), id, prendaId));
    }

    @Override
    @PostMapping("/{id}/like")
    public ResponseEntity<PlaylistResponse> alternarLike(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarLike(validarUsuarioId(usuarioId), id));
    }

    @Override
    @PostMapping("/{id}/guardar")
    public ResponseEntity<PlaylistResponse> alternarGuardado(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarGuardado(validarUsuarioId(usuarioId), id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = "X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        crearPlaylistUseCase.eliminar(validarUsuarioId(usuarioId), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> misPlaylists(
            @RequestHeader(value = "X-User-Id") String usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorUsuario(validarUsuarioId(usuarioId)));
    }

    @Override
    @GetMapping("/me/guardadas")
    public ResponseEntity<List<PlaylistResponse>> misGuardadas(
            @RequestHeader(value = "X-User-Id") String usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerGuardadas(validarUsuarioId(usuarioId)));
    }

    private String validarUsuarioId(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new NoAutorizadoException("La cabecera X-User-Id es obligatoria y no puede estar vacía");
        }
        return usuarioId.trim();
    }
}
JAVA

cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/exception/GlobalExceptionHandler.java
package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.AccesoDenegadoPlaylistException;
import edu.dosw.styleradar.playlist.domain.exception.DomainException;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        if ("X-User-Id".equalsIgnoreCase(ex.getHeaderName())) {
            return problem(HttpStatus.UNAUTHORIZED, "No autorizado", "La cabecera X-User-Id es obligatoria para acceder a este recurso");
        }
        return problem(HttpStatus.BAD_REQUEST, "Cabecera faltante", ex.getMessage());
    }

    @ExceptionHandler(NoAutorizadoException.class)
    public ProblemDetail handleNoAutorizado(NoAutorizadoException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(LimiteDestacadasSuperadoException.class)
    public ProblemDetail handleLimiteDestacadas(LimiteDestacadasSuperadoException ex) {
        return problem(HttpStatus.CONFLICT, "Límite de destacadas superado", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoPlaylistException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoPlaylistException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", "La solicitud tiene campos inválidos");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error interno inesperado en ms-playlist", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        return detail;
    }
}
JAVA

# Sonar fix preventivo: evitar bloque catch vacío en CatalogoEventsConsumer
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/amqp/CatalogoEventsConsumer.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CatalogoEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(CatalogoEventsConsumer.class);
    private final AsociarPrendaUseCase asociarPrendaUseCase;

    public CatalogoEventsConsumer(AsociarPrendaUseCase asociarPrendaUseCase) {
        this.asociarPrendaUseCase = asociarPrendaUseCase;
    }

    @RabbitListener(queues = "${styleradar.rabbitmq.queues.prenda-retirada:styleradar.playlist.prenda-retirada.q}")
    public void handlePrendaRetirada(Map<String, Object> event) {
        if (event != null && event.get("prendaId") != null) {
            try {
                UUID prendaId = UUID.fromString(event.get("prendaId").toString());
                asociarPrendaUseCase.marcarPrendaNoDisponibleEnTodas(prendaId);
            } catch (IllegalArgumentException ex) {
                log.warn("Evento recibido con prendaId inválido: {}", event.get("prendaId"), ex);
            }
        }
    }
}
JAVA

# Sonar fix preventivo: BeanConfig no vacío
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/config/BeanConfig.java
package edu.dosw.styleradar.playlist.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
JAVA

# 8. Crear la nueva Migración Flyway según la secuencia existente
if ls src/main/resources/db/migration/V2__*.sql >/dev/null 2>&1; then
    MIGRATION_FILE="src/main/resources/db/migration/V3__usuario_id_a_texto.sql"
else
    MIGRATION_FILE="src/main/resources/db/migration/V2__usuario_id_a_texto.sql"
fi

echo "==> Creando migración Flyway: $MIGRATION_FILE"
cat << 'SQL' > "$MIGRATION_FILE"
ALTER TABLE playlists
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;

ALTER TABLE playlist_likes
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;

ALTER TABLE playlist_guardados
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;
SQL

# 9. Asegurar application-test.yml limpio para pruebas unitarias e integración
cat << 'YAML' > src/test/resources/application-test.yml
spring:
  application:
    name: ms-playlist-test
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
  rabbitmq:
    ssl:
      enabled: false
    listener:
      simple:
        auto-startup: false
YAML

# 10. Actualizar TODAS las Pruebas Unitarias e Integración (con buenas prácticas SonarQube)
cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/domain/model/PlaylistTest.java
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
    void crearPlaylistValidaConUsuarioNumericoGateway() {
        String usuarioId = "15";
        Playlist p = Playlist.crear(usuarioId, "Look Urbano Bogotá", "Hoodies y joggers", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);

        assertThat(p.getId()).isNotNull();
        assertThat(p.getUsuarioId()).isEqualTo("15");
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
                .isInstanceOf(NoAutorizadoException.class);
        assertThatThrownBy(() -> Playlist.crear("   ", "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL))
                .isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void setNombreVacioOLargoLanzaExcepcion() {
        Playlist p = Playlist.crear("15", "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setNombre(""))
                .isInstanceOf(DomainException.class);
        String largo = "a".repeat(101);
        assertThatThrownBy(() -> p.setNombre(largo))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setDescripcionLargaLanzaExcepcion() {
        Playlist p = Playlist.crear("15", "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        String largo = "a".repeat(301);

        assertThatThrownBy(() -> p.setDescripcion(largo))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaHasta15Exitoso() {
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        for (int i = 1; i <= 15; i++) {
            p.agregarPrenda(new PrendaSnapshot(UUID.randomUUID(), "Prenda " + i, Instant.now()));
        }

        assertThat(p.getTotalPrendas()).isEqualTo(15);
        PrendaSnapshot extra = new PrendaSnapshot(UUID.randomUUID(), "Prenda 16", Instant.now());
        assertThatThrownBy(() -> p.agregarPrenda(extra))
                .isInstanceOf(LimitePrendasAlcanzadoException.class);
    }

    @Test
    void agregarPrendaNulaLanzaExcepcion() {
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        assertThatThrownBy(() -> p.agregarPrenda(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaDuplicadaLanzaExcepcion() {
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        UUID prendaId = UUID.randomUUID();
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Chaqueta", Instant.now()));
        PrendaSnapshot duplicada = new PrendaSnapshot(prendaId, "Chaqueta Otra", Instant.now());

        assertThatThrownBy(() -> p.agregarPrenda(duplicada))
                .isInstanceOf(PrendaDuplicadaException.class);
    }

    @Test
    void retirarPrendaPorPropietario() {
        String usuarioId = "15";
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
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        boolean removida = p.desvincularPrendaSilencioso(prendaId);
        assertThat(removida).isTrue();
        assertThat(p.getTotalPrendas()).isZero();

        boolean segunda = p.desvincularPrendaSilencioso(prendaId);
        assertThat(segunda).isFalse();
        assertThat(p.desvincularPrendaSilencioso(null)).isFalse();
    }

    @Test
    void marcarPrendaNoDisponibleEnPlaylist() {
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        assertThat(p.getPrendas().get(0).isDisponible()).isTrue();
        boolean marcada = p.marcarPrendaNoDisponible(prendaId);
        assertThat(marcada).isTrue();
        assertThat(p.getPrendas().get(0).isDisponible()).isFalse();

        assertThat(p.marcarPrendaNoDisponible(null)).isFalse();
        assertThat(p.marcarPrendaNoDisponible(UUID.randomUUID())).isFalse();
    }

    @Test
    void alternarLikeYGuardado() {
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        p.alternarLike("100");
        assertThat(p.getLikesCount()).isEqualTo(1);
        p.alternarLike("100");
        assertThat(p.getLikesCount()).isZero();

        p.alternarGuardado("200");
        assertThat(p.getGuardadosCount()).isEqualTo(1);
        p.alternarGuardado("200");
        assertThat(p.getGuardadosCount()).isZero();

        assertThatThrownBy(() -> p.alternarLike(null)).isInstanceOf(NoAutorizadoException.class);
        assertThatThrownBy(() -> p.alternarGuardado("  ")).isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void destacarYQuitarDestacado() {
        Playlist p = Playlist.crear("15", "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.destacar();
        assertThat(p.isDestacada()).isTrue();
        p.quitarDestacado();
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void validarAccesoLecturaPrivada() {
        Playlist p = Playlist.crear("15", "Colección Privada", null, VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        p.validarAccesoLectura("15");
        assertThatThrownBy(() -> p.validarAccesoLectura("99"))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
    }

    @Test
    void validarAccesoLecturaPublica() {
        Playlist p = Playlist.crear("15", "Colección Pública", null, VisibilidadPlaylist.PUBLICA, Estilo.FORMAL);
        p.validarAccesoLectura(null);
        p.validarAccesoLectura("99");
        assertThat(p.getNombre()).isEqualTo("Colección Pública");
    }

    @Test
    void editarPlaylistExitoso() {
        Playlist p = Playlist.crear("15", "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.editar("15", "Modificado", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        assertThat(p.getNombre()).isEqualTo("Modificado");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
    }

    @Test
    void editarPlaylistUsuarioAjenoLanzaExcepcion() {
        Playlist p = Playlist.crear("15", "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        assertThatThrownBy(() -> p.editar("99", "Modificado", null, null, null))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
    }

    @Test
    void retirarPrendaInvalidaLanzaExcepciones() {
        Playlist p = Playlist.crear("15", "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        UUID prendaId = UUID.randomUUID();
        assertThatThrownBy(() -> p.retirarPrenda(prendaId, "99"))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
        assertThatThrownBy(() -> p.retirarPrenda(null, "15"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void constructorCompletoConValidaciones() {
        UUID id = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();

        Playlist p = new Playlist(
                id, "15", "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.DEPORTIVO, true,
                Instant.now(), Instant.now(), List.of(new PrendaSnapshot(prendaId, "p", Instant.now())),
                Set.of("100"), Set.of("200")
        );

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsuarioId()).isEqualTo("15");
        assertThat(p.isDestacada()).isTrue();
        assertThat(p.getTotalPrendas()).isEqualTo(1);
        assertThat(p.getLikesCount()).isEqualTo(1);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
    }

    @Test
    void settersYGetters() {
        Playlist p = new Playlist();
        UUID id = UUID.randomUUID();
        p.setId(id);
        p.setUsuarioId("42");
        p.setVisibilidad(VisibilidadPlaylist.PRIVADA);
        p.setEstilo(Estilo.FORMAL);
        p.setDestacada(true);
        p.setFechaActualizacion(Instant.now());

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsuarioId()).isEqualTo("42");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
        assertThat(p.isDestacada()).isTrue();
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/application/usecase/UseCasesTest.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {

    @Mock
    private PlaylistRepositoryPort repository;

    @Mock
    private PlaylistEventPublisherPort eventPublisher;

    private final PlaylistDtoMapper mapper = Mappers.getMapper(PlaylistDtoMapper.class);

    private CrearPlaylistUseCaseImpl crearUseCase;
    private ConsultarPlaylistUseCaseImpl consultarUseCase;
    private DestacarPlaylistUseCaseImpl destacarUseCase;
    private AsociarPrendaUseCaseImpl asociarPrendaUseCase;
    private InteractuarPlaylistUseCaseImpl interactuarUseCase;

    @BeforeEach
    void setUp() {
        crearUseCase = new CrearPlaylistUseCaseImpl(repository, mapper);
        consultarUseCase = new ConsultarPlaylistUseCaseImpl(repository, mapper);
        destacarUseCase = new DestacarPlaylistUseCaseImpl(repository, eventPublisher, mapper);
        asociarPrendaUseCase = new AsociarPrendaUseCaseImpl(repository, eventPublisher, mapper);
        interactuarUseCase = new InteractuarPlaylistUseCaseImpl(repository, mapper);
    }

    @Test
    void crearYEditarPlaylist() {
        String usuarioId = "15";
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(playlistId);

        when(repository.save(any(Playlist.class))).thenReturn(p);
        when(repository.findById(playlistId)).thenReturn(Optional.of(p));

        PlaylistResponse resCrear = crearUseCase.crear(usuarioId, new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO));
        assertThat(resCrear.id()).isEqualTo(playlistId);
        assertThat(resCrear.usuarioId()).isEqualTo("15");

        PlaylistResponse resEditar = crearUseCase.editar(usuarioId, playlistId, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL));
        assertThat(resEditar.nombre()).isEqualTo("Nuevo");

        crearUseCase.eliminar(usuarioId, playlistId);
        verify(repository).delete(playlistId);
    }

    @Test
    void consultasPlaylist() {
        String usuarioId = "15";
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.findAllPublicas()).thenReturn(List.of(p));
        when(repository.findPopulares(0, 10)).thenReturn(List.of(p));
        when(repository.countPublicas()).thenReturn(1L);
        when(repository.findByUsuarioId(usuarioId)).thenReturn(List.of(p));
        when(repository.findByGuardadosUsuarioId(usuarioId)).thenReturn(List.of(p));
        when(repository.findDestacadas()).thenReturn(List.of(p));

        assertThat(consultarUseCase.obtenerPorId(playlistId, usuarioId).id()).isEqualTo(playlistId);
        assertThat(consultarUseCase.obtenerPublicas()).hasSize(1);
        assertThat(consultarUseCase.obtenerPopulares(0, 10).content()).hasSize(1);
        assertThat(consultarUseCase.obtenerPorUsuario(usuarioId)).hasSize(1);
        assertThat(consultarUseCase.obtenerGuardadas(usuarioId)).hasSize(1);
        assertThat(consultarUseCase.obtenerDestacadas()).hasSize(1);
    }

    @Test
    void destacarPlaylistValidaTope() {
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear("15", "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(3L);

        assertThatThrownBy(() -> destacarUseCase.destacar("1", playlistId))
                .isInstanceOf(LimiteDestacadasSuperadoException.class);
    }

    @Test
    void asociarYRetirarPrenda() {
        String usuarioId = "15";
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = asociarPrendaUseCase.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Gorra"));
        assertThat(res.totalPrendas()).isEqualTo(1);

        PlaylistResponse resRetiro = asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId);
        assertThat(resRetiro.totalPrendas()).isZero();

        when(repository.findAllConPrenda(prendaId)).thenReturn(List.of(p));
        asociarPrendaUseCase.desvincularPrendaDeTodas(prendaId);
        asociarPrendaUseCase.marcarPrendaNoDisponibleEnTodas(prendaId);
    }

    @Test
    void interactuarLikeYGuardado() {
        String usuarioId = "15";
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        interactuarUseCase.alternarLike(usuarioId, playlistId);
        interactuarUseCase.alternarGuardado(usuarioId, playlistId);

        verify(repository, times(2)).save(p);
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/rest/PlaylistControllerTest.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock private CrearPlaylistUseCase crearPlaylistUseCase;
    @Mock private ConsultarPlaylistUseCase consultarPlaylistUseCase;
    @Mock private DestacarPlaylistUseCase destacarPlaylistUseCase;
    @Mock private AsociarPrendaUseCase asociarPrendaUseCase;
    @Mock private InteractuarPlaylistUseCase interactuarPlaylistUseCase;

    private PlaylistController controller;
    private MockMvc mockMvc;
    private PlaylistResponse sampleResponse;
    private final String usuarioId = "15";
    private final UUID playlistId = UUID.randomUUID();
    private final UUID prendaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new PlaylistController(crearPlaylistUseCase, consultarPlaylistUseCase, destacarPlaylistUseCase, asociarPrendaUseCase, interactuarPlaylistUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        sampleResponse = new PlaylistResponse(
                playlistId, usuarioId, "Streetwear", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, false,
                Instant.now(), List.of(), 0, 0, 0
        );
    }

    @Test
    void peticionProtegidaSinHeaderOBlandRetorna401() throws Exception {
        mockMvc.perform(get("/playlists/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/playlists/me").header("X-User-Id", "   "))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Urbano\",\"visibilidad\":\"PUBLICA\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPlaylistConUsuarioEnteroDeGatewayRetorna201() throws Exception {
        when(crearPlaylistUseCase.crear(eq("15"), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/playlists")
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Streetwear\",\"descripcion\":\"Desc\",\"visibilidad\":\"PUBLICA\",\"estilo\":\"URBANO\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void obtenerPorIdRetorna200() {
        when(consultarPlaylistUseCase.obtenerPorId(playlistId, usuarioId)).thenReturn(sampleResponse);
        ResponseEntity<PlaylistResponse> response = controller.obtenerPorId(playlistId, usuarioId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obtenerPublicasYPopulares() {
        when(consultarPlaylistUseCase.obtenerPublicas()).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerPopulares(0, 20)).thenReturn(new PageResponse<>(List.of(sampleResponse), 0, 20, 1L, 1, true, true));

        assertThat(controller.obtenerPublicas().getBody()).hasSize(1);
        assertThat(controller.obtenerPopulares(0, 20).getBody().content()).hasSize(1);
    }

    @Test
    void obtenerDestacadasRetornaLista() {
        when(consultarPlaylistUseCase.obtenerDestacadas()).thenReturn(List.of(sampleResponse));
        assertThat(controller.obtenerDestacadas().getBody()).hasSize(1);
    }

    @Test
    void editarPlaylistRetorna200() {
        when(crearPlaylistUseCase.editar(eq(usuarioId), eq(playlistId), any())).thenReturn(sampleResponse);
        ResponseEntity<PlaylistResponse> response = controller.editar(usuarioId, playlistId, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.CASUAL));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void destacarYQuitarDestacado() {
        when(destacarPlaylistUseCase.destacar(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(destacarPlaylistUseCase.quitarDestacado(usuarioId, playlistId)).thenReturn(sampleResponse);

        assertThat(controller.destacar(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.quitarDestacado(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void agregarYRetirarPrenda() {
        when(asociarPrendaUseCase.agregarPrenda(eq(usuarioId), eq(playlistId), any())).thenReturn(sampleResponse);
        when(asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId)).thenReturn(sampleResponse);

        assertThat(controller.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Gorra")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.retirarPrenda(usuarioId, playlistId, prendaId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void alternarLikeYGuardado() {
        when(interactuarPlaylistUseCase.alternarLike(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(interactuarPlaylistUseCase.alternarGuardado(usuarioId, playlistId)).thenReturn(sampleResponse);

        assertThat(controller.alternarLike(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.alternarGuardado(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminarRetorna204() {
        ResponseEntity<Void> response = controller.eliminar(usuarioId, playlistId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(crearPlaylistUseCase).eliminar(usuarioId, playlistId);
    }

    @Test
    void misPlaylistsYMisGuardadas() {
        when(consultarPlaylistUseCase.obtenerPorUsuario(usuarioId)).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerGuardadas(usuarioId)).thenReturn(List.of(sampleResponse));

        assertThat(controller.misPlaylists(usuarioId).getBody()).hasSize(1);
        assertThat(controller.misGuardadas(usuarioId).getBody()).hasSize(1);
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/PostgreSQLPlaylistRepositoryAdapterTest.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.mapper.PlaylistEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPlaylistRepositoryAdapterTest {

    @Mock
    private SpringDataPlaylistRepository springDataRepository;

    private final PlaylistEntityMapper mapper = Mappers.getMapper(PlaylistEntityMapper.class);

    private PostgreSQLPlaylistRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgreSQLPlaylistRepositoryAdapter(springDataRepository, mapper);
    }

    @Test
    void guardarYRecuperarPlaylist() {
        UUID id = UUID.randomUUID();
        String usuarioId = "15";
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(id);
        entity.setUsuarioId(usuarioId);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.save(any())).thenReturn(entity);
        when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(id);
        Playlist guardada = adapter.save(p);
        Optional<Playlist> opt = adapter.findById(id);

        assertThat(guardada.getId()).isEqualTo(id);
        assertThat(guardada.getUsuarioId()).isEqualTo("15");
        assertThat(opt).isPresent();
    }

    @Test
    void consultasMultiples() {
        UUID id = UUID.randomUUID();
        String usuarioId = "15";
        UUID prendaId = UUID.randomUUID();
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(id);
        entity.setUsuarioId(usuarioId);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)).thenReturn(List.of(entity));
        when(springDataRepository.findPopulares(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA)).thenReturn(1L);
        when(springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)).thenReturn(List.of(entity));
        when(springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(usuarioId)).thenReturn(List.of(entity));
        when(springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()).thenReturn(List.of(entity));
        when(springDataRepository.countByDestacadaTrue()).thenReturn(1L);
        when(springDataRepository.findAllByPrendaId(prendaId)).thenReturn(List.of(entity));

        assertThat(adapter.findAllPublicas()).hasSize(1);
        assertThat(adapter.findPopulares(0, 10)).hasSize(1);
        assertThat(adapter.countPublicas()).isEqualTo(1L);
        assertThat(adapter.findByUsuarioId(usuarioId)).hasSize(1);
        assertThat(adapter.findByGuardadosUsuarioId(usuarioId)).hasSize(1);
        assertThat(adapter.findDestacadas()).hasSize(1);
        assertThat(adapter.countDestacadas()).isEqualTo(1L);
        assertThat(adapter.findAllConPrenda(prendaId)).hasSize(1);

        adapter.delete(id);
        verify(springDataRepository).deleteById(id);
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/exception/GlobalExceptionHandlerTest.java
package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingUserIdHeaderReturns401() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("missingUserIdHeaderReturns401"), -1);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-User-Id", parameter);
        ProblemDetail detail = handler.handleMissingHeader(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void blankUserIdHeaderReturns401() {
        NoAutorizadoException ex = new NoAutorizadoException("Cabecera vacía");
        ProblemDetail detail = handler.handleNoAutorizado(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void notFoundReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No existe");
        ProblemDetail detail = handler.handleNotFound(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void limiteDestacadasReturns409() {
        LimiteDestacadasSuperadoException ex = new LimiteDestacadasSuperadoException("Máx 3");
        ProblemDetail detail = handler.handleLimiteDestacadas(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void accesoDenegadoReturns403() {
        AccesoDenegadoPlaylistException ex = new AccesoDenegadoPlaylistException("Privada");
        ProblemDetail detail = handler.handleAccesoDenegado(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void domainErrorReturns422() {
        DomainException ex = new DomainException("Regla incumplida");
        ProblemDetail detail = handler.handleDomain(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void validationErrorReturns400WithFields() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void unexpectedErrorReturns500() {
        Exception ex = new IllegalStateException("fallo");
        ProblemDetail detail = handler.handleUnexpected(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/PlaylistServiceApplicationIntegrationTest.java
package edu.dosw.styleradar.playlist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PlaylistServiceApplicationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ApplicationContext context;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }
}
JAVA

echo "=========================================================================="
echo "  CORRIENDO VERIFICACIÓN COMPLETA (mvn clean verify)                      "
echo "=========================================================================="

if [ -f "./mvnw" ]; then
    ./mvnw clean verify
else
    mvn clean verify
fi
