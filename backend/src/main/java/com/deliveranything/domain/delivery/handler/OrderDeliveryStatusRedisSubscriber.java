package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.notification.repository.EmitterRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryStatusRedisSubscriber {

  private final EmitterRepository emitterRepository;
  private final ObjectMapper objectMapper;

  @RedisListener(channel = "order-delivery-status")
  public void handleMessage(String message) {
    try {
      OrderStatusUpdateEvent event = objectMapper.readValue(message, OrderStatusUpdateEvent.class);
      // 1) 라이더 본인에게 전송
      emitterRepository.sendToAll(event.riderId(), "ORDER_STATUS", event);
      // 2) 관련 주문자에게 전송
      Long customerId = getCustomerIdByOrderId(event.orderId()); // DB 조회 또는 캐시
      if (customerId != null) {
        emitterRepository.sendToAll(customerId, "ORDER_STATUS", event);
      }
      // 3) 관련 상점에게 전송
      Long storeId = getStoreIdByOrderId(event.orderId());
      if (storeId != null) {
        emitterRepository.sendToAll(storeId, "ORDER_STATUS", event);
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  // 주문자/스토어 조회는 서비스 호출이나 캐시 활용
  private Long getCustomerIdByOrderId(String orderId) { /* ... */
    return 123L;
  }

  private Long getStoreIdByOrderId(String orderId) { /* ... */
    return 456L;
  }
}
