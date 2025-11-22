package com.deliveranything.global.enums;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisTopic {

  // Payment Events
  PAYMENT_COMPLETED_EVENT("payment-completed-event"),
  PAYMENT_FAILED_EVENT("payment-failed-event"),
  PAYMENT_CANCEL_SUCCESS_EVENT("payment-cancel-success-event"),
  PAYMENT_CANCEL_FAILED_EVENT("payment-cancel-failed-event"),

  // Stock Events
  STOCK_RESERVED_EVENT("stock-reserved-event"),
  STOCK_RESERVE_FAILED_EVENT("stock-reserve-failed-event"),
  STOCK_COMMITTED_EVENT("stock-committed-event"),
  STOCK_RELEASED_EVENT("stock-released-event"),
  STOCK_REPLENISHED_EVENT("stock-replenished-event"),

  // Order Events
  ORDER_CREATED_EVENT("order-created-event"),
  ORDER_PAYMENT_REQUESTED_EVENT("order-payment-requested-event"),
  ORDER_PAYMENT_SUCCEEDED_EVENT("order-payment-succeeded-event"), // Corrected from 'Succeeded'
  ORDER_REJECTED_EVENT("order-rejected-event"),
  ORDER_STORE_ACCEPTED_EVENT("order-store-accepted-event"),
  ORDER_COMPLETED_EVENT("order-completed-event"),
  ORDER_CANCEL_EVENT("order-cancel-event"),
  ORDER_CANCEL_SUCCEEDED_EVENT("order-cancel-succeeded-event"),
  ORDER_RIDER_ACCEPTED_EVENT("order-rider-accepted-event"),
  ORDER_PAID_FOR_CUSTOMER_EVENT("order-paid-for-customer-event"),
  ORDER_PAID_FOR_SELLER_EVENT("order-paid-for-seller-event"),
  ORDER_PAYMENT_FAILED_FOR_CUSTOMER_EVENT("order-payment-failed-for-customer-event"),
  ORDER_CANCELED_FOR_CUSTOMER_EVENT("order-canceled-for-customer-event"),
  ORDER_CANCELED_FOR_SELLER_EVENT("order-canceled-for-seller-event"),
  ORDER_CANCEL_FAILED_FOR_CUSTOMER_EVENT("order-cancel-failed-for-customer-event"),
  ORDER_CANCEL_FAILED_FOR_SELLER_EVENT("order-cancel-failed-for-seller-event"),
  ORDER_STATUS_CHANGED_FOR_CUSTOMER_EVENT("order-status-changed-for-customer-event"),
  ORDER_STATUS_CHANGED_FOR_SELLER_EVENT("order-status-changed-for-seller-event"),
  ORDER_PREPARING_FOR_CUSTOMER_EVENT("order-preparing-for-customer-event"),
  ORDER_PREPARING_FOR_SELLER_EVENT("order-preparing-for-seller-event"),
  ORDER_CREATED_FOR_CUSTOMER_EVENT("order-created-for-customer-event"),
  ORDER_CREATED_FAILED_FOR_CUSTOMER_EVENT("order-created-failed-for-customer-event"),

  // Delivery Events
  DELIVERY_STATUS_EVENT("delivery-status-event"),
  DELIVERY_OFFERED_TO_RIDERS_EVENT("delivery-offered-to-riders-event");

  private final String topic;

  private static final Map<String, RedisTopic> topicMap = Stream.of(values())
      .collect(Collectors.toMap(RedisTopic::getTopic, Function.identity()));

  public static RedisTopic fromTopic(String topic) {
    return topicMap.get(topic);
  }
}
