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
        Queue prendaRetiradaQueue = config.prendaRetiradaQueue();
        Queue dlq = config.playlistDlq();
        Binding binding = config.prendaRetiradaBinding(prendaRetiradaQueue);
        Binding dlqBinding = config.playlistDlqBinding(dlq);

        assertThat(mainExchange.getName()).isEqualTo("styleradar.exchange");
        assertThat(mainExchange.isDurable()).isTrue();
        assertThat(binding.getExchange()).isEqualTo("styleradar.exchange");
        assertThat(dlqBinding.getExchange()).isEqualTo("styleradar.dlx");
        assertThat(prendaRetiradaQueue.getName()).isEqualTo("styleradar.playlist.prenda-retirada.q");
        assertThat(prendaRetiradaQueue.getArguments()).containsEntry("x-dead-letter-exchange", "styleradar.dlx");
        assertThat(prendaRetiradaQueue.getArguments()).containsEntry("x-dead-letter-routing-key", "styleradar.playlist.dlq");
        assertThat(dlq.getName()).isEqualTo("styleradar.playlist.dlq");
        assertThat(binding.getRoutingKey()).isEqualTo("styleradar.catalogo.prenda.retirada");
        assertThat(dlqBinding.getRoutingKey()).isEqualTo("styleradar.playlist.dlq");

        MessageConverter converter = config.jacksonMessageConverter();
        assertThat(converter).isNotNull();
        assertThat(config.rabbitTemplate(mock(ConnectionFactory.class), converter)).isNotNull();
        assertThat(config.rabbitAdmin(mock(ConnectionFactory.class))).hasFieldOrPropertyWithValue("ignoreDeclarationExceptions", true);
    }
}
