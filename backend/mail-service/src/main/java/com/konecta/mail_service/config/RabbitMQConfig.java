package com.konecta.mail_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.otp-queue}")
    private String otpQueue;

    @Value("${app.rabbitmq.otp-exchange}")
    private String otpExchange;

    @Value("${app.rabbitmq.otp-routing-key}")
    private String otpRoutingKey;

    @Value("${app.rabbitmq.welcome-queue}")
    private String welcomeQueue;

    @Value("${app.rabbitmq.welcome-exchange}")
    private String welcomeExchange;

    @Value("${app.rabbitmq.welcome-routing-key}")
    private String welcomeRoutingKey;

    @Bean
    public Queue otpQueue() {
        return new Queue(otpQueue);
    }

    @Bean
    public TopicExchange otpExchange() {
        return new TopicExchange(otpExchange);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(otpQueue()).to(otpExchange()).with(otpRoutingKey);
    }

    @Bean
    public Queue welcomeQueue() {
        return new Queue(welcomeQueue);
    }

    @Bean
    public TopicExchange welcomeExchange() {
        return new TopicExchange(welcomeExchange);
    }

    @Bean
    public Binding welcomeBinding() {
        return BindingBuilder.bind(welcomeQueue()).to(welcomeExchange()).with(welcomeRoutingKey);
    }


    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}