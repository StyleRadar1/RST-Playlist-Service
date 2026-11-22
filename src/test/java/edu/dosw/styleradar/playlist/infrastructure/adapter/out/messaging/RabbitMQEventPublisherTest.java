package edu.dosw.styleradar.playlist.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publicarEventosConPrefijoStyleRadar() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();

        publisher.publicarPlaylistDestacadaActualizada(playlistId, true);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.playlist.destacada.actualizada"), any(Object.class));

        publisher.publicarPrendaAsociada(playlistId, prendaId);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.playlist.prenda.asociada"), any(Object.class));
    }

    @Test
    void sinBrokerNoFallaLaOperacion() {
        RabbitMQEventPublisher publisher = new RabbitMQEventPublisher(rabbitTemplate);
        doThrow(new AmqpConnectException(new java.net.ConnectException("Connection refused")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertThatCode(() -> publisher.publicarPrendaAsociada(UUID.randomUUID(), UUID.randomUUID())).doesNotThrowAnyException();
        assertThatCode(() -> publisher.publicarPlaylistDestacadaActualizada(UUID.randomUUID(), true)).doesNotThrowAnyException();
    }
}
