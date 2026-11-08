package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publicarPlaylistDestacadaActualizada() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        publisher.publicarPlaylistDestacadaActualizada(1L, true);

        verify(rabbitTemplate).convertAndSend(eq("styleradar.events"), eq("playlist.destacada.actualizada"), any(Object.class));
    }

    @Test
    void publicarPrendaAsociada() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        publisher.publicarPrendaAsociada(1L, 10L);

        verify(rabbitTemplate).convertAndSend(eq("styleradar.events"), eq("playlist.prenda.asociada"), any(Object.class));
    }
}
