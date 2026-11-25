package edu.dosw.styleradar.playlist.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
    public Queue prendaRetiradaQueue() {
        return QueueBuilder.durable(QUEUE_PRENDA_RETIRADA)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_PLAYLIST)
                .build();
    }

    @Bean
    public Queue playlistDlq() {
        return QueueBuilder.durable(DLQ_PLAYLIST).build();
    }

    @Bean
    public Binding prendaRetiradaBinding(Queue prendaRetiradaQueue) {
        return new Binding(prendaRetiradaQueue.getName(), Binding.DestinationType.QUEUE,
                MAIN_EXCHANGE, ROUTING_KEY_PRENDA_RETIRADA, null);
    }

    @Bean
    public Binding playlistDlqBinding(Queue playlistDlq) {
        return new Binding(playlistDlq.getName(), Binding.DestinationType.QUEUE,
                DLX_EXCHANGE, DLQ_PLAYLIST, null);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter() {
            @Override
            protected org.springframework.amqp.core.Message createMessage(Object objectToConvert, org.springframework.amqp.core.MessageProperties messageProperties) {
                org.springframework.amqp.core.Message message = super.createMessage(objectToConvert, messageProperties);
                message.getMessageProperties().getHeaders().remove("__TypeId__");
                return message;
            }
        };
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }
}
