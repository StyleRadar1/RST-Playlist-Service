package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.playlist.application.port.out.PlaylistEventPublisherPort;
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
    public void publicarPlaylistDestacadaActualizada(Long playlistId, boolean destacada) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "playlist.destacada.actualizada",
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId, "destacada", destacada)
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "playlist.destacada.actualizada", payload);
    }

    @Override
    public void publicarPrendaAsociada(Long playlistId, Long prendaId) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "playlist.prenda.asociada",
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId, "prendaId", prendaId)
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "playlist.prenda.asociada", payload);
    }
}
