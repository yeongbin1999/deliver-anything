package com.deliveranything.domain.delivery.handler.redis;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.delivery.event.event.sse.OrderAssignmentSsePublisher;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
// 로드 밸런싱 / 다중 인스턴스 환경 대응하여 Redis Pub/Sub 구축
public class OrderAssignmentRedisSubscriber implements MessageListener {

  private static final String CHANNEL = "order-assignment-events";
  private final ObjectMapper objectMapper;
  private final RedisMessageListenerContainer container;
  private final OrderAssignmentSsePublisher orderAssignmentSsePublisher;

  @PostConstruct
  public void subscribe() {
    container.addMessageListener(this, new PatternTopic(CHANNEL));
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String channel = new String(message.getChannel());
      String body = new String(message.getBody());

      if (CHANNEL.equals(channel)) {
        RiderNotificationDto event = objectMapper.readValue(body, RiderNotificationDto.class);
        orderAssignmentSsePublisher.publish(event);
      }
    } catch (Exception e) {
      throw new CustomException(ErrorCode.REDIS_MESSAGE_PROCESSING_ERROR);
    }
  }
}
