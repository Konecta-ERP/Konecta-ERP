package com.konecta.identity_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.otp-exchange}")
    private String otpExchange;

    @Bean
    public TopicExchange otpExchange() {
        return new TopicExchange(otpExchange);
    }
}