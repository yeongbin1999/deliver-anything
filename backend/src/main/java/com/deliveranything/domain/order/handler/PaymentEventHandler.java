package com.deliveranything.domain.order.handler;

import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.payment.event.PaymentCancelFailedEvent;
import com.deliveranything.domain.payment.event.PaymentCancelSuccessEvent;
import com.deliveranything.domain.payment.event.PaymentFailedEvent;
import com.deliveranything.domain.payment.event.PaymentSuccessEvent;
import com.deliveranything.global.enums.RedisTopic;
import com.deliveranything.global.event.RedisEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler implements RedisEventHandler {

  private final ObjectMapper objectMapper;
  private final OrderService orderService;

  @Override
  public void handle(RedisTopic topic, String json) {
    try {
      switch (topic) {
        case PAYMENT_COMPLETED_EVENT -> {
          PaymentSuccessEvent event = objectMapper.readValue(json, PaymentSuccessEvent.class);
          orderService.processPaymentCompletion(event.merchantUid());
        }
        case PAYMENT_FAILED_EVENT -> {
          PaymentFailedEvent event = objectMapper.readValue(json, PaymentFailedEvent.class);
          orderService.processPaymentFailure(event.merchantUid());
        }
        case PAYMENT_CANCEL_SUCCESS_EVENT -> {
          PaymentCancelSuccessEvent event = objectMapper.readValue(json,
              PaymentCancelSuccessEvent.class);
          orderService.processPaymentCancelSuccess(event.merchantUid(), event.publisher());
        }
        case PAYMENT_CANCEL_FAILED_EVENT -> {
          PaymentCancelFailedEvent event = objectMapper.readValue(json,
              PaymentCancelFailedEvent.class);
          orderService.processPaymentCancelFailed(event.merchantId());
        }
        default -> log.warn("Unhandled payment event topic: {}", topic);
      }
    } catch (Exception e) {
      log.error("Failed to process payment event in order [{}]: {}", topic, e.getMessage(), e);
    }
  }
}
