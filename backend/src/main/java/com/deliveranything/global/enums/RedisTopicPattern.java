package com.deliveranything.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisTopicPattern {

  PAYMENT_EVENTS("payment-*-event"),
  STOCK_EVENTS("stock-*-event"),
  DELIVERY_EVENTS("delivery-*-event"),
  ORDER_EVENTS("order-*-event");

  private final String pattern;
}
