package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class RabbitMQEventPublisher implements PlaylistEventPublisherPort {

    public static final String EXCHANGE = "styleradar.exchange";
    public static final String ROUTING_KEY_PLAYLIST_DESTACADA = "styleradar.playlist.destacada.actualizada";
    public static final String ROUTING_KEY_PRENDA_ASOCIADA = "styleradar.playlist.prenda.asociada";

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarPlaylistDestacadaActualizada(UUID playlistId, boolean destacada) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", ROUTING_KEY_PLAYLIST_DESTACADA,
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId.toString(), "destacada", destacada)
        );
        enviar(ROUTING_KEY_PLAYLIST_DESTACADA, payload);
    }

    @Override
    public void publicarPrendaAsociada(UUID playlistId, UUID prendaId) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", ROUTING_KEY_PRENDA_ASOCIADA,
                "timestamp", Instant.now().toString(),
                "producer", "ms-playlist",
                "data", Map.of("playlistId", playlistId.toString(), "prendaId", prendaId.toString())
        );
        enviar(ROUTING_KEY_PRENDA_ASOCIADA, payload);
    }

    private void enviar(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (AmqpException e) {
            log.warn("No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}
