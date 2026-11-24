package com.rickrone.logiflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  public static final String ORDER_EXCHANGE = "logiflow.order.direct";
  public static final String ORDER_CREATE_QUEUE = "logiflow.order.create.queue";
  public static final String ORDER_CREATE_ROUTING_KEY = "order.create";

  @Bean
  public DirectExchange orderExchange() {
    return new DirectExchange(ORDER_EXCHANGE, true, false);
  }

  @Bean
  public Queue orderCreateQueue() {
    return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
  }

  @Bean
  public Binding orderCreateBinding(Queue orderCreateQueue, DirectExchange orderExchange) {
    return BindingBuilder.bind(orderCreateQueue)
            .to(orderExchange)
            .with(ORDER_CREATE_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
