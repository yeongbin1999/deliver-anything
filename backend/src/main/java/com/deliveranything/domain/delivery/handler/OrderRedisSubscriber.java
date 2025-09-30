package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderDeliveryCreatedEvent;
import com.deliveranything.domain.delivery.event.event.redis.OrderAssignmentRedisPublisher;
import com.deliveranything.domain.delivery.service.OrderNotificationService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
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
public class OrderRedisSubscriber implements MessageListener {

  // 주문 도메인에서 주문 생성을 발행한 redis 채널
  private static final String CHANNEL = "order-delivery-created";
  private final ObjectMapper objectMapper;
  private final RedisMessageListenerContainer container;
  private final OrderNotificationService orderNotificationService;
  private final OrderAssignmentRedisPublisher orderAssignmentRedisPublisher;

  @PostConstruct
  public void subscribe() {
    container.addMessageListener(this, new PatternTopic(CHANNEL));
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      OrderDeliveryCreatedEvent event = objectMapper.readValue(body,
          OrderDeliveryCreatedEvent.class);

      // orderNotificationService.processOrderEvent() 과정 진행
      // 반경 내 라이더 ETA 조회 후 주문 정보, 라이더 Id, eta minute, 주문 상태 포함한 List<RiderNotificationDto> 생성
      orderNotificationService.processOrderEvent(event)
          .subscribe(orderAssignmentRedisPublisher::publish); // 자동으로 OrderAssignment Redis 발행

    } catch (Exception e) {
      throw new CustomException(ErrorCode.REDIS_MESSAGE_PROCESSING_ERROR);
    }
  }
}
