package com.deliveranything.domain.product.stock.subscriber;

import com.deliveranything.domain.order.event.OrderCancelEvent;
import com.deliveranything.domain.order.event.OrderCancelSucceededEvent;
import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.order.event.OrderPaymentFailedEvent;
import com.deliveranything.domain.order.event.OrderPaymentSucceededEvent;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import com.deliveranything.domain.product.stock.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProcessor implements MessageListener {

  private final RedisMessageListenerContainer container;
  private final ObjectMapper objectMapper;
  private final StockService stockService;

  @PostConstruct
  public void registerListener() {
    container.addMessageListener(this, new PatternTopic("order-created-event"));
    container.addMessageListener(this, new PatternTopic("order-cancel-event"));
    container.addMessageListener(this, new PatternTopic("order-cancel-succeeded-event"));
    container.addMessageListener(this, new PatternTopic("order-payment-succeeded-event"));
    container.addMessageListener(this, new PatternTopic("order-rejected-event"));
    container.addMessageListener(this, new PatternTopic("order-payment-failed-event"));
  }

  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    String channel = new String(pattern);
    String json = new String(message.getBody());

    try {
      if ("order-created-event".equals(channel)) {
        OrderCreatedEvent event = objectMapper.readValue(json, OrderCreatedEvent.class);
        stockService.reserveStock(event.orderId(), event.orderItems());
      } else if ("order-payment-succeeded-event".equals(channel)) {
        OrderPaymentSucceededEvent event = objectMapper.readValue(json, OrderPaymentSucceededEvent.class);
        stockService.commitStock(event.orderId(), event.orderItems());
      } else if ("order-cancel-event".equals(channel)) {
        OrderCancelEvent event = objectMapper.readValue(json, OrderCancelEvent.class);
        stockService.releaseStock(event.orderId(), event.orderItems());
      } else if ("order-cancel-succeeded-event".equals(channel)) {
        OrderCancelSucceededEvent event = objectMapper.readValue(json, OrderCancelSucceededEvent.class);
        stockService.releaseStock(event.orderId(), event.orderItems());
      } else if ("order-rejected-event".equals(channel)) {
        OrderRejectedEvent event = objectMapper.readValue(json, OrderRejectedEvent.class);
        stockService.releaseStockForRejectedOrder(event.orderId());
      } else if ("order-payment-failed-event".equals(channel)) {
        OrderPaymentFailedEvent event = objectMapper.readValue(json, OrderPaymentFailedEvent.class);
        stockService.releaseStock(event.orderId(), event.orderItems());
      }
    } catch (Exception e) {
      log.error("Failed to process order event from Redis for channel {}: {}", channel, e.getMessage(), e);
    }
  }
}