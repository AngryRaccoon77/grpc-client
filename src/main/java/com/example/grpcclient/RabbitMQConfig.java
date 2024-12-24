package com.example.grpcclient;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final String ALERT_QUEUE_NAME = "alertQueue";
    private static final String ALERT_EXCHANGE_NAME = "alertExchange";
    private static final String DEVICE_QUEUE_NAME = "deviceStatusQueue2";
    private static final String DEVICE_EXCHANGE_NAME = "deviceExchange";

    @Bean
    public Queue alertQueue() {
        return new Queue(ALERT_QUEUE_NAME, false);
    }

    @Bean
    public Exchange alertExchange() {
        return new TopicExchange(ALERT_EXCHANGE_NAME);
    }

    @Bean
    public Binding alertBinding(Queue alertQueue, Exchange alertExchange) {
        return BindingBuilder.bind(alertQueue).to(alertExchange).with("alerts.routingKey").noargs();
    }

    @Bean
    public Queue deviceQueue() {
        return new Queue(DEVICE_QUEUE_NAME, false);
    }

    @Bean
    public Exchange deviceExchange() {
        return new TopicExchange(DEVICE_EXCHANGE_NAME);
    }

    @Bean
    public Binding deviceBinding(Queue deviceQueue, Exchange deviceExchange) {
        return BindingBuilder.bind(deviceQueue).to(deviceExchange).with("devices.routingKey").noargs();
    }
}