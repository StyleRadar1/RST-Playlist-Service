package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @SuppressWarnings("unchecked")
    void publicarEventosConPrefijoStyleRadar() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();

        publisher.publicarPlaylistCreada(playlistId);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.playlist.creada"), captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(UUID.fromString(payload.get("eventId").toString())).isNotNull();
        assertThat(payload.get("eventType")).isEqualTo("styleradar.playlist.creada");
        assertThat(Instant.parse(payload.get("occurredAt").toString())).isNotNull();
        assertThat(payload.get("producer")).isEqualTo("ms-playlist");

        publisher.publicarPlaylistDestacadaActualizada(playlistId, true);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.playlist.destacada.actualizada"), any(Object.class));

        publisher.publicarPrendaAsociada(playlistId, prendaId);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.playlist.prenda.asociada"), any(Object.class));
    }
}
