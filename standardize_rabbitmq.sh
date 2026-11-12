#!/usr/bin/env bash
set -e

echo "=========================================================================="
echo "  ESTANDARIZACIÓN RABBITMQ ms-playlist (EXCHANGE, DLX Y DISPONIBILIDAD)   "
echo "=========================================================================="

# 0. Asegurar que todas las carpetas existan
mkdir -p src/main/java/edu/dosw/styleradar/playlist/domain/model
mkdir -p src/main/java/edu/dosw/styleradar/playlist/domain/port/in
mkdir -p src/main/java/edu/dosw/styleradar/playlist/domain/port/out
mkdir -p src/main/java/edu/dosw/styleradar/playlist/application/dto
mkdir -p src/main/java/edu/dosw/styleradar/playlist/application/usecase
mkdir -p src/main/java/edu/dosw/styleradar/playlist/infrastructure/config
mkdir -p src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/amqp
mkdir -p src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/messaging
mkdir -p src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/entity
mkdir -p src/main/resources/db/migration

mkdir -p src/test/java/edu/dosw/styleradar/playlist/domain/model
mkdir -p src/test/java/edu/dosw/styleradar/playlist/application/usecase
mkdir -p src/test/java/edu/dosw/styleradar/playlist/infrastructure/config
mkdir -p src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/amqp
mkdir -p src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/messaging

# 1. PrendaSnapshot (con soporte de disponibilidad)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/model/PrendaSnapshot.java
package edu.dosw.styleradar.playlist.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PrendaSnapshot {
    private final UUID prendaId;
    private final String nombrePrenda;
    private final Instant fechaAsociacion;
    private boolean disponible;

    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true);
    }

    public PrendaSnapshot(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        if (prendaId == null) {
            throw new IllegalArgumentException("El ID de la prenda no puede ser nulo");
        }
        this.prendaId = prendaId;
        this.nombrePrenda = (nombrePrenda != null && !nombrePrenda.isBlank()) ? nombrePrenda.trim() : "Prenda #" + prendaId;
        this.fechaAsociacion = (fechaAsociacion != null) ? fechaAsociacion : Instant.now();
        this.disponible = disponible;
    }

    public void marcarNoDisponible() {
        this.disponible = false;
    }

    public void marcarDisponible() {
        this.disponible = true;
    }

    public UUID getPrendaId() { return prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrendaSnapshot that = (PrendaSnapshot) o;
        return Objects.equals(prendaId, that.prendaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prendaId);
    }
}
JAVA

# 2. Playlist Aggregate (con marcarPrendaNoDisponible)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/model/Playlist.java
package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;

import java.time.Instant;
import java.util.*;

public class Playlist {
    public static final int MAX_PRENDAS = 15; // RNF-12
    public static final int MAX_DESTACADAS = 3; // SR-RF-72

    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String descripcion;
    private VisibilidadPlaylist visibilidad;
    private Estilo estilo;
    private boolean destacada;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    private final List<PrendaSnapshot> prendas = new ArrayList<>();
    private final Set<UUID> likesUsuarios = new HashSet<>();
    private final Set<UUID> guardadosUsuarios = new HashSet<>();

    public Playlist() {
        this.id = UUID.randomUUID();
        this.visibilidad = VisibilidadPlaylist.PUBLICA;
        this.estilo = Estilo.URBANO;
        this.destacada = false;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = Instant.now();
    }

    public Playlist(UUID id, UUID usuarioId, String nombre, String descripcion,
                    VisibilidadPlaylist visibilidad, Estilo estilo, boolean destacada,
                    Instant fechaCreacion, Instant fechaActualizacion,
                    List<PrendaSnapshot> prendas, Set<UUID> likesUsuarios, Set<UUID> guardadosUsuarios) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.usuarioId = usuarioId;
        setNombre(nombre);
        setDescripcion(descripcion);
        this.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        this.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        this.destacada = destacada;
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : Instant.now();
        this.fechaActualizacion = (fechaActualizacion != null) ? fechaActualizacion : this.fechaCreacion;
        setPrendas(prendas);
        setLikesUsuarios(likesUsuarios);
        setGuardadosUsuarios(guardadosUsuarios);
    }

    public static Playlist crear(UUID usuarioId, String nombre, String descripcion,
                                 VisibilidadPlaylist visibilidad, Estilo estilo) {
        if (usuarioId == null) {
            throw new DomainException("El ID del usuario creador es obligatorio");
        }
        Playlist p = new Playlist();
        p.id = UUID.randomUUID();
        p.usuarioId = usuarioId;
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
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
            throw new PrendaDuplicadaException("La prenda ya se encuentra asociada a este lookbook");
        }
        this.prendas.add(prenda);
        this.fechaActualizacion = Instant.now();
    }

    public void retirarPrenda(UUID prendaId, UUID usuarioEjecutorId) {
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

    public void alternarLike(UUID usuarioId) {
        if (usuarioId == null) {
            throw new DomainException("El ID de usuario es obligatorio");
        }
        if (this.likesUsuarios.contains(usuarioId)) {
            this.likesUsuarios.remove(usuarioId);
        } else {
            this.likesUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void alternarGuardado(UUID usuarioId) {
        if (usuarioId == null) {
            throw new DomainException("El ID de usuario es obligatorio");
        }
        if (this.guardadosUsuarios.contains(usuarioId)) {
            this.guardadosUsuarios.remove(usuarioId);
        } else {
            this.guardadosUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void editar(UUID usuarioId, String nombre, String descripcion,
                       VisibilidadPlaylist visibilidad, Estilo estilo) {
        validarPropietario(usuarioId);
        setNombre(nombre);
        setDescripcion(descripcion);
        if (visibilidad != null) {
            this.visibilidad = visibilidad;
        }
        if (estilo != null) {
            this.estilo = estilo;
        }
        this.fechaActualizacion = Instant.now();
    }

    public void validarPropietario(UUID usuarioId) {
        if (usuarioId == null || !usuarioId.equals(this.usuarioId)) {
            throw new AccesoDenegadoPlaylistException("No tienes permisos para modificar este lookbook");
        }
    }

    public void validarAccesoLectura(UUID usuarioConsultaId) {
        if (this.visibilidad == VisibilidadPlaylist.PRIVADA) {
            if (usuarioConsultaId == null || !usuarioConsultaId.equals(this.usuarioId)) {
                throw new AccesoDenegadoPlaylistException("No tienes permisos para consultar esta colección privada");
            }
        }
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre del lookbook es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new DomainException("El nombre no puede superar los 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 300) {
            throw new DomainException("La descripción no puede superar los 300 caracteres");
        }
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
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
    public void setPrendas(List<PrendaSnapshot> prendas) {
        this.prendas.clear();
        if (prendas != null) {
            if (prendas.size() > MAX_PRENDAS) {
                throw new LimitePrendasAlcanzadoException("La playlist no admite más de " + MAX_PRENDAS + " prendas (RNF-12)");
            }
            this.prendas.addAll(prendas);
        }
    }

    public Set<UUID> getLikesUsuarios() { return Collections.unmodifiableSet(likesUsuarios); }
    public void setLikesUsuarios(Set<UUID> likes) {
        this.likesUsuarios.clear();
        if (likes != null) this.likesUsuarios.addAll(likes);
    }

    public Set<UUID> getGuardadosUsuarios() { return Collections.unmodifiableSet(guardadosUsuarios); }
    public void setGuardadosUsuarios(Set<UUID> guardados) {
        this.guardadosUsuarios.clear();
        if (guardados != null) this.guardadosUsuarios.addAll(guardados);
    }

    public int getLikesCount() { return likesUsuarios.size(); }
    public int getGuardadosCount() { return guardadosUsuarios.size(); }
    public int getTotalPrendas() { return prendas.size(); }
}
JAVA

# 3. Puerto de entrada AsociarPrendaUseCase
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/domain/port/in/AsociarPrendaUseCase.java
package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface AsociarPrendaUseCase {
    PlaylistResponse agregarPrenda(UUID usuarioId, UUID playlistId, AgregarPrendaRequest request);
    PlaylistResponse retirarPrenda(UUID usuarioId, UUID playlistId, UUID prendaId);
    void desvincularPrendaDeTodas(UUID prendaId);
    void marcarPrendaNoDisponibleEnTodas(UUID prendaId);
}
JAVA

# 4. Implementación AsociarPrendaUseCaseImpl
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
    public PlaylistResponse agregarPrenda(UUID usuarioId, UUID playlistId, AgregarPrendaRequest request) {
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
    public PlaylistResponse retirarPrenda(UUID usuarioId, UUID playlistId, UUID prendaId) {
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

# 5. PrendaResponse DTO (con campo disponible)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/application/dto/PrendaResponse.java
package edu.dosw.styleradar.playlist.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PrendaResponse(
    UUID prendaId,
    String nombrePrenda,
    Instant fechaAsociacion,
    boolean disponible
) {}
JAVA

# 6. PrendaEmbeddable para persistencia JPA
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/persistence/entity/PrendaEmbeddable.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.UUID;

@Embeddable
public class PrendaEmbeddable {

    @Column(name = "prenda_id", nullable = false)
    private UUID prendaId;

    @Column(name = "nombre_prenda", length = 150)
    private String nombrePrenda;

    @Column(name = "fecha_asociacion")
    private Instant fechaAsociacion;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;

    public PrendaEmbeddable() {}

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion, boolean disponible) {
        this.prendaId = prendaId;
        this.nombrePrenda = nombrePrenda;
        this.fechaAsociacion = fechaAsociacion;
        this.disponible = disponible;
    }

    public PrendaEmbeddable(UUID prendaId, String nombrePrenda, Instant fechaAsociacion) {
        this(prendaId, nombrePrenda, fechaAsociacion, true);
    }

    public UUID getPrendaId() { return prendaId; }
    public void setPrendaId(UUID prendaId) { this.prendaId = prendaId; }
    public String getNombrePrenda() { return nombrePrenda; }
    public void setNombrePrenda(String nombrePrenda) { this.nombrePrenda = nombrePrenda; }
    public Instant getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(Instant fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
JAVA

# 7. Migración Flyway V1__init_schema.sql
cat << 'SQL' > src/main/resources/db/migration/V1__init_schema.sql
CREATE TABLE IF NOT EXISTS playlists (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLICA',
    estilo VARCHAR(30) NOT NULL DEFAULT 'URBANO',
    destacada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS playlist_prendas (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    prenda_id UUID NOT NULL,
    nombre_prenda VARCHAR(150),
    fecha_asociacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disponible BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS playlist_likes (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS playlist_guardados (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, usuario_id)
);
SQL

# 8. RabbitMqConfig (styleradar.exchange, styleradar.dlx, cola con DLX y DLQ)
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/config/RabbitMqConfig.java
package edu.dosw.styleradar.playlist.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String MAIN_EXCHANGE = "styleradar.exchange";
    public static final String DLX_EXCHANGE = "styleradar.dlx";
    public static final String QUEUE_PRENDA_RETIRADA = "styleradar.playlist.prenda-retirada.q";
    public static final String DLQ_PLAYLIST = "styleradar.playlist.dlq";
    public static final String ROUTING_KEY_PRENDA_RETIRADA = "styleradar.catalogo.prenda.retirada";

    @Bean
    public TopicExchange mainExchange() {
        return ExchangeBuilder.topicExchange(MAIN_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue prendaRetiradaQueue() {
        return QueueBuilder.durable(QUEUE_PRENDA_RETIRADA)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue playlistDlq() {
        return QueueBuilder.durable(DLQ_PLAYLIST).build();
    }

    @Bean
    public Binding prendaRetiradaBinding(Queue prendaRetiradaQueue, TopicExchange mainExchange) {
        return BindingBuilder.bind(prendaRetiradaQueue)
                .to(mainExchange)
                .with(ROUTING_KEY_PRENDA_RETIRADA);
    }

    @Bean
    public Binding playlistDlqBinding(Queue playlistDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(playlistDlq)
                .to(deadLetterExchange)
                .with("#");
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }
}
JAVA

# 9. CatalogoEventsConsumer escuchando en styleradar.playlist.prenda-retirada.q
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/amqp/CatalogoEventsConsumer.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CatalogoEventsConsumer {

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
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
JAVA

# 10. RabbitMQEventPublisher publicando en styleradar.exchange
cat << 'JAVA' > src/main/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/messaging/RabbitMQEventPublisher.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class RabbitMQEventPublisher implements PlaylistEventPublisherPort {

    static final String EXCHANGE = "styleradar.exchange";
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarPlaylistDestacadaActualizada(UUID playlistId, boolean destacada) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "playlist.destacada.actualizada",
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId.toString(), "destacada", destacada)
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "playlist.destacada.actualizada", payload);
    }

    @Override
    public void publicarPrendaAsociada(UUID playlistId, UUID prendaId) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "playlist.prenda.asociada",
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId.toString(), "prendaId", prendaId.toString())
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "playlist.prenda.asociada", payload);
    }
}
JAVA

# 11. application.yml con configuración estandarizada de RabbitMQ
cat << 'YAML' > src/main/resources/application.yml
server:
  port: ${SERVER_PORT:8260}
  servlet:
    context-path: /api/v1

spring:
  application:
    name: ms-playlist
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:playlist_db}
    username: ${DB_USER:styleradar_user}
    password: ${DB_PASSWORD:}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:}

styleradar:
  rabbitmq:
    exchanges:
      main: styleradar.exchange
      dlx: styleradar.dlx
    queues:
      prenda-retirada: styleradar.playlist.prenda-retirada.q
      dlq: styleradar.playlist.dlq
    routing-keys:
      prenda-retirada: styleradar.catalogo.prenda.retirada

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha

management:
  endpoints:
    web:
      exposure:
        include: health, info
YAML

# 12. Pruebas Unitarias Actualizadas
cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/config/RabbitMqConfigTest.java
package edu.dosw.styleradar.playlist.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMqConfigTest {

    private final RabbitMqConfig config = new RabbitMqConfig();

    @Test
    void validaConfiguracionExchangesYColas() {
        TopicExchange mainExchange = config.mainExchange();
        TopicExchange dlx = config.deadLetterExchange();
        Queue prendaRetiradaQueue = config.prendaRetiradaQueue();
        Queue dlq = config.playlistDlq();
        Binding binding = config.prendaRetiradaBinding(prendaRetiradaQueue, mainExchange);
        Binding dlqBinding = config.playlistDlqBinding(dlq, dlx);

        assertThat(mainExchange.getName()).isEqualTo("styleradar.exchange");
        assertThat(dlx.getName()).isEqualTo("styleradar.dlx");
        assertThat(prendaRetiradaQueue.getName()).isEqualTo("styleradar.playlist.prenda-retirada.q");
        assertThat(prendaRetiradaQueue.getArguments()).containsEntry("x-dead-letter-exchange", "styleradar.dlx");
        assertThat(dlq.getName()).isEqualTo("styleradar.playlist.dlq");
        assertThat(binding.getRoutingKey()).isEqualTo("styleradar.catalogo.prenda.retirada");
        assertThat(dlqBinding.getRoutingKey()).isEqualTo("#");

        MessageConverter converter = config.jacksonMessageConverter();
        assertThat(converter).isNotNull();
        assertThat(config.rabbitTemplate(mock(ConnectionFactory.class), converter)).isNotNull();
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/in/amqp/CatalogoEventsConsumerTest.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalogoEventsConsumerTest {

    @Mock
    private AsociarPrendaUseCase asociarPrendaUseCase;

    @Test
    void consumeEventoPrendaRetiradaCorrectamente() {
        UUID prendaId = UUID.randomUUID();
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(asociarPrendaUseCase);
        consumer.handlePrendaRetirada(Map.of("prendaId", prendaId.toString()));

        verify(asociarPrendaUseCase).marcarPrendaNoDisponibleEnTodas(prendaId);
    }

    @Test
    void consumeEventoInvalidoNoFalla() {
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(asociarPrendaUseCase);
        consumer.handlePrendaRetirada(null);
        consumer.handlePrendaRetirada(Map.of("prendaId", "no-es-uuid"));

        verifyNoInteractions(asociarPrendaUseCase);
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/infrastructure/adapter/out/messaging/RabbitMQEventPublisherTest.java
package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publicarEventosConUUID() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();

        publisher.publicarPlaylistDestacadaActualizada(playlistId, true);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("playlist.destacada.actualizada"), any(Object.class));

        publisher.publicarPrendaAsociada(playlistId, prendaId);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("playlist.prenda.asociada"), any(Object.class));
    }
}
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/domain/model/PrendaSnapshotTest.java
package edu.dosw.styleradar.playlist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrendaSnapshotTest {

    @Test
    void prendaSnapshotValidaYDisponibilidad() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        PrendaSnapshot p1 = new PrendaSnapshot(id, "Hoodie", now);

        assertThat(p1.getPrendaId()).isEqualTo(id);
        assertThat(p1.getNombrePrenda()).isEqualTo("Hoodie");
        assertThat(p1.getFechaAsociacion()).isEqualTo(now);
        assertThat(p1.isDisponible()).isTrue();

        p1.marcarNoDisponible();
        assertThat(p1.isDisponible()).isFalse();

        p1.marcarDisponible();
        assertThat(p1.isDisponible()).isTrue();

        p1.setDisponible(false);
        assertThat(p1.isDisponible()).isFalse();
    }

    @Test
    void prendaSnapshotInvalidaLanzaExcepcion() {
        assertThatThrownBy(() -> new PrendaSnapshot(null, "Hoodie", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
JAVA

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
    void marcarPrendaNoDisponibleEnPlaylist() {
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
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
JAVA

cat << 'JAVA' > src/test/java/edu/dosw/styleradar/playlist/application/usecase/UseCasesTest.java
package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(playlistId);

        when(repository.save(any(Playlist.class))).thenReturn(p);
        when(repository.findById(playlistId)).thenReturn(Optional.of(p));

        PlaylistResponse resCrear = crearUseCase.crear(usuarioId, new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO));
        assertThat(resCrear.id()).isEqualTo(playlistId);

        PlaylistResponse resEditar = crearUseCase.editar(usuarioId, playlistId, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL));
        assertThat(resEditar.nombre()).isEqualTo("Nuevo");

        crearUseCase.eliminar(usuarioId, playlistId);
        verify(repository).delete(playlistId);
    }

    @Test
    void consultasPlaylist() {
        UUID usuarioId = UUID.randomUUID();
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
        Playlist p = Playlist.crear(UUID.randomUUID(), "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(3L);

        assertThatThrownBy(() -> destacarUseCase.destacar(UUID.randomUUID(), playlistId))
                .isInstanceOf(LimiteDestacadasSuperadoException.class);
    }

    @Test
    void asociarYRetirarPrenda() {
        UUID usuarioId = UUID.randomUUID();
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
        UUID usuarioId = UUID.randomUUID();
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

echo "=========================================================================="
echo "  COMPILANDO Y CORRIENDO PRUEBAS UNITARIAS...                             "
echo "=========================================================================="

if [ -f "./mvnw" ]; then
    ./mvnw clean test
else
    mvn clean test
fi

echo "=========================================================================="
echo "  ¡COMPILÓ Y PASARON TODAS LAS PRUEBAS CON ÉXITO!                         "
echo "=========================================================================="
