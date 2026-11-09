package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class RabbitMQEventPublisher implements PlaylistEventPublisherPort {

    static final String EXCHANGE = "styleradar.events";
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
