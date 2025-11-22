package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.DeliveryEventHandler;
import com.deliveranything.global.enums.RedisTopic;
import com.deliveranything.global.enums.RedisTopicPattern;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventSubscriber implements MessageListener {

  private final RedisMessageListenerContainer container;
  private final DeliveryEventHandler deliveryEventHandler;

  @PostConstruct
  public void registerListener() {
    container.addMessageListener(this,
        new PatternTopic(RedisTopicPattern.DELIVERY_EVENTS.getPattern()));
  }

  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    String topicStr = new String(message.getChannel());
    String json = new String(message.getBody());
    log.debug("Received Redis event topic={}, body={}", topicStr, json);

    RedisTopic topic = RedisTopic.fromTopic(topicStr);
    if (topic == null) {
      log.warn("Unknown topic received: {}", topicStr);
      return;
    }

    deliveryEventHandler.handle(topic, json);
  }
}
