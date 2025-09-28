package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.domain.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryStatusRedisSubscriber implements MessageListener {

  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final RedisMessageListenerContainer container;
  private final OrderService orderService;

  @PostConstruct
  public void subscribe() {
    container.addMessageListener(this, new PatternTopic("order-delivery-status"));
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      OrderStatusUpdateEvent event = objectMapper.readValue(body, OrderStatusUpdateEvent.class);

      // 1) 라이더 본인에게 전송
      notificationService.sendToAll(event.riderId(), "ORDER_STATUS", event);

      // 2) 관련 주문자에게 전송
      Long customerId = getCustomerIdByOrderId(event.orderId()); // DB 조회 또는 캐시
      if (customerId != null) {
        notificationService.sendToAll(customerId, "ORDER_STATUS", event);
      }

      // 3) 관련 상점에게 전송
      Long storeId = getStoreIdByOrderId(event.orderId());
      if (storeId != null) {
        notificationService.sendToAll(storeId, "ORDER_STATUS", event);
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  // 주문자/스토어 조회는 서비스 호출이나 캐시 활용
  private Long getCustomerIdByOrderId(String orderId) { /* ... */
    return orderService.getCustomerIdByOrderId(Long.parseLong(orderId));
  }

  private Long getStoreIdByOrderId(String orderId) { /* ... */
    return orderService.getStoreIdByOrderId(Long.parseLong(orderId));
  }
}
