package com.deliveranything.domain.payment.handler;

import com.deliveranything.domain.order.enums.Publisher;
import com.deliveranything.domain.order.event.OrderCancelEvent;
import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.global.enums.RedisTopic;
import com.deliveranything.global.event.RedisEventHandler;
import com.deliveranything.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler implements RedisEventHandler {

  private final ObjectMapper objectMapper;
  private final PaymentService paymentService;

  @Override
  public void handle(RedisTopic topic, String json) {
    try {
      switch (topic) {
        case ORDER_PAYMENT_REQUESTED_EVENT -> {
          OrderPaymentRequestedEvent event = objectMapper.readValue(json,
              OrderPaymentRequestedEvent.class);
          try {
            paymentService.createPayment(event.paymentKey(), event.merchantUid(), event.amount());
            paymentService.confirmPayment(event.paymentKey(), event.merchantUid(), event.amount());
          } catch (CustomException e) {
            log.warn("Payment failed for order {}: {}", event.orderId(), e.getMessage());
          }
        }
        case ORDER_CANCEL_EVENT -> {
          OrderCancelEvent event = objectMapper.readValue(json, OrderCancelEvent.class);
          handlePaymentCancellation(event.orderId(), event.merchantUid(), event.cancelReason(),
              event.publisher());
        }
        case ORDER_REJECTED_EVENT -> {
          OrderRejectedEvent event = objectMapper.readValue(json, OrderRejectedEvent.class);
          handlePaymentCancellation(event.orderId(), event.merchantUid(), event.cancelReason(),
              event.publisher());
        }
        default -> log.warn("Unhandled order event topic in payment: {}", topic);
      }
    } catch (Exception e) {
      log.error("Failed to process order event in payment [{}]: {}", topic, e.getMessage(), e);
    }
  }

  private void handlePaymentCancellation(
      Long orderId,
      String merchantUid,
      String cancelReason,
      Publisher publisher
  ) {
    try {
      paymentService.cancelPayment(merchantUid, cancelReason, publisher);
    } catch (CustomException e) {
      log.warn("Payment cancel failed for order {}: {}", orderId, e.getMessage());
    }
  }
}
