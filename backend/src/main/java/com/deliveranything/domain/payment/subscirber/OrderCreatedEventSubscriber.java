package com.deliveranything.domain.payment.subscirber;

import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventSubscriber implements MessageListener {

  private final RedisMessageListenerContainer container;
  private final ObjectMapper objectMapper;
  private final PaymentService paymentService;

  @PostConstruct
  public void registerListener() {
    container.addMessageListener(this, new ChannelTopic("order-created-event"));
  }

  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    try {
      OrderCreatedEvent event = objectMapper.readValue(new String(message.getBody()),
          OrderCreatedEvent.class);
      paymentService.createPayment(event.merchantUid(), event.totalPrice());
    } catch (Exception e) {
      log.error("Failed to process order created event from Redis", e);
    }
  }
}
