package com.deliveranything.domain.delivery.handler.redis;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.delivery.event.event.sse.OrderDeliveryStatusSsePublisher;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.delivery.service.DeliveryService;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
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
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderDeliveryStatusRedisSubscriber implements MessageListener {

  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final RedisMessageListenerContainer container;
  private final OrderService orderService;
  private final DeliveryRepository deliveryRepository;
  private final DeliveryService deliveryService;
  private final OrderDeliveryStatusSsePublisher orderDeliveryStatusSsePublisher;

  @PostConstruct
  public void subscribe() {
    container.addMessageListener(this, new PatternTopic("order-delivery-status"));
  }

  @Override
  @Transactional
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      OrderStatusUpdateEvent event = objectMapper.readValue(body, OrderStatusUpdateEvent.class);

      // 1️⃣ 상태 변경 처리 (이벤트 기반)
      handleStatusChange(event);
      // 2️⃣ 알림 전송
      sendNotifications(event);

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  // 상태 변경 처리
  private void handleStatusChange(OrderStatusUpdateEvent event) {
    String orderId = event.orderId();
    Long riderId = event.riderId();

    // 라이더 수락 시 Delivery 생성
    if (event.status().name().equals("RIDER_ASSIGNED")) {
      Order order = orderService.getOrderById(Long.parseLong(orderId));
      order.updateStatus(OrderStatus.RIDER_ASSIGNED);

      // Delivery 생성
      Delivery delivery = deliveryService.createDelivery(order, riderId, event.eta());
      deliveryRepository.save(delivery);
    }
  }

  // 알림 전송
  private void sendNotifications(OrderStatusUpdateEvent event) {
    // 1) 라이더 본인에게 전송
    orderDeliveryStatusSsePublisher.publish(event.riderId(), event);

    // 2) 관련 주문자에게 전송
    Long customerId = getCustomerIdByOrderId(event.orderId());
    if (customerId != null) {
      orderDeliveryStatusSsePublisher.publish(customerId, event);
    }

    // 3) 관련 상점에게 전송
    Long sellerId = getStoreIdByOrderId(event.orderId());
    if (sellerId != null) {
      orderDeliveryStatusSsePublisher.publish(sellerId, event);
    }
  }

  // 주문자/스토어 조회는 서비스 호출이나 캐시 활용
  private Long getCustomerIdByOrderId(String orderId) { /* ... */
    return orderService.getCustomerIdByOrderId(Long.parseLong(orderId));
  }

  private Long getStoreIdByOrderId(String orderId) { /* ... */
    return orderService.getSellerIdByOrderId(Long.parseLong(orderId));
  }
}