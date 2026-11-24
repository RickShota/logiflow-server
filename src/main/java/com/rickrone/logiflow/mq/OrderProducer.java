package com.rickrone.logiflow.mq;

import com.rickrone.logiflow.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
  private final RabbitTemplate rabbitTemplate;
  public void sendOrderCreateMessage(OrderMessage msg) {
    log.info("发送订单创建异步消息，orderNo={}", msg.getOrderNo());
    rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
            msg);
  }
}
