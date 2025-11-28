package com.deliveranything.global.event;

import com.deliveranything.global.enums.RedisTopic;

public interface RedisEventHandler {

  void handle(RedisTopic topic, String json);
}