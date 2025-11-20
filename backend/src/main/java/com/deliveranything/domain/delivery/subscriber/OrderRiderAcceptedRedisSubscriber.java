package com.deliveranything.domain.delivery.subscriber;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.delivery.service.DeliveryService;
import com.deliveranything.domain.notification.subscriber.delivery.OrderRiderDecisionNotifier;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.event.OrderRiderAcceptedEvent;
import com.deliveranything.domain.order.service.DeliveryOrderService;
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
public class OrderRiderAcceptedRedisSubscriber implements MessageListener {

  private final ObjectMapper objectMapper;
  private final RedisMessageListenerContainer container;
  private final DeliveryOrderService deliveryOrderService;
  private final DeliveryRepository deliveryRepository;
  private final DeliveryService deliveryService;
  private final OrderRiderDecisionNotifier orderRiderDecisionNotifier;

  @PostConstruct
  public void subscribe() {
    container.addMessageListener(this, new PatternTopic("order-rider-accepted-event"));
  }

  @Override
  @Transactional
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      OrderRiderAcceptedEvent event = objectMapper.readValue(body, OrderRiderAcceptedEvent.class);

      // 1️⃣ 상태 변경 처리 (이벤트 기반)
      handleStatusChange(event);
      // 2️⃣ 알림 전송
      sendNotifications(event);

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  // 상태 변경 처리
  private void handleStatusChange(OrderRiderAcceptedEvent event) {
    Order order = deliveryOrderService.getOrderById(event.orderId());

    // TODO: Delivery Customer, Store 다 객체가 아닌 Id로 저장하게끔
    //  (객체 자체를 갖고와서 타 객체 생성에 넣게 되면 결합도가 증가함)
    // 라이더 수락 시 Delivery 생성
    if (event.status().name().equals("RIDER_ASSIGNED")) {
      Delivery delivery = deliveryService.createDelivery(order, event.riderId(), event.eta());
      deliveryRepository.save(delivery);
    }
  }

  // 알림 전송
  private void sendNotifications(OrderRiderAcceptedEvent event) {
    // 1) 라이더 본인에게 전송
    orderRiderDecisionNotifier.publish(event.riderId(), event);

    // 2) 관련 주문자에게 전송
    Long customerId = getCustomerIdByOrderId(event.orderId());
    if (customerId != null) {
      orderRiderDecisionNotifier.publish(customerId, event);
    }

    // 3) 관련 상점에게 전송
    Long sellerId = getStoreIdByOrderId(event.orderId());
    if (sellerId != null) {
      orderRiderDecisionNotifier.publish(sellerId, event);
    }
  }

  // 주문자/스토어 조회는 서비스 호출이나 캐시 활용
  private Long getCustomerIdByOrderId(Long orderId) {
    return deliveryOrderService.getCustomerIdByOrderId(orderId);
  }

  private Long getStoreIdByOrderId(Long orderId) {
    return deliveryOrderService.getSellerIdByOrderId(orderId);
  }
}