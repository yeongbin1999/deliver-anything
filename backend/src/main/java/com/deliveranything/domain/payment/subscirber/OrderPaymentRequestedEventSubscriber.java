package com.deliveranything.domain.payment.subscirber;

import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.payment.event.PaymentFailedEvent;
import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentRequestedEventSubscriber implements MessageListener {

  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final PaymentService paymentService;
  private final RedisMessageListenerContainer container;

  @PostConstruct
  public void registerListener() {
    container.addMessageListener(this, new ChannelTopic("order-payment-requested-event"));
  }

  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    OrderPaymentRequestedEvent event = null;
    try {
      event = objectMapper.readValue(new String(message.getBody()),
          OrderPaymentRequestedEvent.class);
      paymentService.confirmPayment(event.paymentKey(), event.merchantUid(), event.amount());
    } catch (CustomException e) {
      if (event != null) {
        log.warn("Payment failed for order {}: {}", event.orderId(), e.getMessage());
        eventPublisher.publishEvent(new PaymentFailedEvent(event.merchantUid()));
      } else {
        log.error("Failed to get payment request message from Redis", e);
      }
    } catch (Exception e) {
      log.error("Failed to process order payment requested event from Redis", e);
    }
  }
}
