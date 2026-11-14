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
