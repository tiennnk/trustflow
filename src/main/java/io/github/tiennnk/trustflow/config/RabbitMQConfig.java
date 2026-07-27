package io.github.tiennnk.trustflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VERIFICATION_EXCHANGE = "verification.events";
    public static final String VERIFICATION_QUEUE = "verification.events.log";

    @Bean
    public TopicExchange verificationExchange() {
        return new TopicExchange(VERIFICATION_EXCHANGE);
    }

    @Bean
    public Queue verificationQueue() {
        return new Queue(VERIFICATION_QUEUE);
    }

    @Bean
    public Binding verificationBinding(Queue verificationQueue, TopicExchange verificationExchange) {
        return BindingBuilder.bind(verificationQueue).to(verificationExchange).with("verification.*");
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
