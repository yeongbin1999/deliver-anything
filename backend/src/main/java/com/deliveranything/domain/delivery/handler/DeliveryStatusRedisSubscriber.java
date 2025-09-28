package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.event.DeliveryStatusSsePublisher;
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
public class DeliveryStatusRedisSubscriber implements MessageListener {

  private static final String CHANNEL = "delivery-status-events";

  private final ObjectMapper objectMapper;
  private final DeliveryStatusSsePublisher ssePublisher;
  private final RedisMessageListenerContainer container;

  @PostConstruct
  public void subscribe() {
    // 애플리케이션 기동 시 구독 시작
    container.addMessageListener(this, new PatternTopic(CHANNEL));
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String json = new String(message.getBody());
      DeliveryStatusEvent event = objectMapper.readValue(json, DeliveryStatusEvent.class);
      // 여기서 최종 SSE 푸시
      ssePublisher.publish(event);
    } catch (Exception e) {
      // TODO: 로깅
    }
  }
}