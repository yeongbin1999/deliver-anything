package com.deliveranything.domain.order.handler;

import com.deliveranything.global.enums.RedisTopic;

public interface RedisEventHandler {

  void handle(RedisTopic topic, String json);
}