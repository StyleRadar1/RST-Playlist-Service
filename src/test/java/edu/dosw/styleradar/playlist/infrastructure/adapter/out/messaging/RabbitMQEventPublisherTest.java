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
        verify(rabbitTemplate).convertAndSend(eq("styleradar.events"), eq("playlist.destacada.actualizada"), any(Object.class));

        publisher.publicarPrendaAsociada(playlistId, prendaId);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.events"), eq("playlist.prenda.asociada"), any(Object.class));
    }
}
