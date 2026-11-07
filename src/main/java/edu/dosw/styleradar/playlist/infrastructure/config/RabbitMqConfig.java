package edu.dosw.styleradar.playlist.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "styleradar.events";

    @Value("${styleradar.rabbitmq.queues.prenda-eliminada:q.playlist.prenda-eliminada}")
    private String queuePrendaEliminada;

    @Bean
    public TopicExchange styleradarExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue prendaEliminadaQueue() {
        return QueueBuilder.durable(queuePrendaEliminada).build();
    }

    @Bean
    public Binding bindingPrendaEliminada(Queue prendaEliminadaQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(prendaEliminadaQueue)
                .to(styleradarExchange)
                .with("catalogo.prenda.eliminada");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}