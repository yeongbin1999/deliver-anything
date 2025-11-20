package com.deliveranything.domain.order.handler;

import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.DeliveryOfferedToRidersEvent;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventHandler {

  private final ObjectMapper objectMapper;
  private final OrderService orderService;

  public void handle(String topic, String json) {
    try {
      switch (topic) {
        case "delivery-offered-to-riders-event" -> {
          log.info("라이더에게 상점이 수락한 주문이 제안됨");

          DeliveryOfferedToRidersEvent event = objectMapper.readValue(json,
              DeliveryOfferedToRidersEvent.class);
          orderService.processOrderTransmitted(event.orderId());
        }
        case "delivery-status-event" -> {
          DeliveryStatusEvent event = objectMapper.readValue(json, DeliveryStatusEvent.class);
          if (event.status() == DeliveryStatus.PICKED_UP) {
            orderService.processDeliveryPickedUp(event.orderId());
          } else if (event.status() == DeliveryStatus.COMPLETED) {
            orderService.processDeliveryCompleted(event.orderId(), event.riderProfileId(),
                event.sellerProfileId());
          }
        }
        default -> log.warn("Unknown topic: {}", topic);
      }
    } catch (Exception e) {
      log.error("Failed to process delivery event in order [{}]: {}", topic, e.getMessage(), e);
    }
  }
}
