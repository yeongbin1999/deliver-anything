package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.RedisEventHandler;
import com.deliveranything.global.enums.RedisTopic;
import com.deliveranything.global.enums.RedisTopicPattern;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;

@Slf4j
public abstract class AbstractRedisEventSubscriber implements MessageListener {

  @Autowired
  private RedisMessageListenerContainer container;

  @PostConstruct
  public void registerListener() {
    container.addMessageListener(this, new PatternTopic(getTopicPattern().getPattern()));
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
    getHandler().handle(topic, json);
  }

  protected abstract RedisTopicPattern getTopicPattern();

  protected abstract RedisEventHandler getHandler();
}
