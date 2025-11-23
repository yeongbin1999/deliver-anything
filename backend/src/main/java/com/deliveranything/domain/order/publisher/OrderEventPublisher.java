package com.deliveranything.domain.order.publisher;

import com.deliveranything.domain.order.event.OrderCancelEvent;
import com.deliveranything.domain.order.event.OrderCancelSucceededEvent;
import com.deliveranything.domain.order.event.OrderCompletedEvent;
import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.order.event.OrderPaymentFailedEvent;
import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.order.event.OrderPaymentSucceededEvent;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import com.deliveranything.domain.order.event.OrderRiderAcceptedEvent;
import com.deliveranything.domain.order.event.OrderStoreAcceptedEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderCancelFailedForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderCanceledForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderCreateFailedForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderCreatedForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderPaidForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderPaymentFailedForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderPreparingForCustomerEvent;
import com.deliveranything.domain.order.event.sse.customer.OrderStatusChangedForCustomerEvent;
import com.deliveranything.domain.order.event.sse.seller.OrderCancelFailedForSellerEvent;
import com.deliveranything.domain.order.event.sse.seller.OrderCanceledForSellerEvent;
import com.deliveranything.domain.order.event.sse.seller.OrderPaidForSellerEvent;
import com.deliveranything.domain.order.event.sse.seller.OrderPreparingForSellerEvent;
import com.deliveranything.domain.order.event.sse.seller.OrderStatusChangedForSellerEvent;
import com.deliveranything.global.enums.RedisTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CREATED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaymentRequestedEvent(OrderPaymentRequestedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAYMENT_REQUESTED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaymentSucceededEvent(OrderPaymentSucceededEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAYMENT_SUCCEEDED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaymentFailedEvent(OrderPaymentFailedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAYMENT_FAILED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderRejectedEvent(OrderRejectedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_REJECTED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderAcceptedEvent(OrderStoreAcceptedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_STORE_ACCEPTED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCompletedEvent(OrderCompletedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_COMPLETED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCancelEvent(OrderCancelEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCEL_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCancelSucceededEvent(OrderCancelSucceededEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCEL_SUCCEEDED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderRiderAcceptedEvent(OrderRiderAcceptedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_RIDER_ACCEPTED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaidForCustomerEvent(OrderPaidForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAID_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaidForSellerEvent(OrderPaidForSellerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAID_FOR_SELLER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaymentFailedForCustomerEvent(OrderPaymentFailedForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PAYMENT_FAILED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCanceledForCustomerEvent(OrderCanceledForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCELED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCanceledForSellerEvent(OrderCanceledForSellerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCELED_FOR_SELLER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCancelFailedForCustomerEvent(OrderCancelFailedForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCEL_FAILED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCancelFailedForSellerEvent(OrderCancelFailedForSellerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CANCEL_FAILED_FOR_SELLER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderStatusChangedForCustomerEvent(OrderStatusChangedForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_STATUS_CHANGED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderStatusChangedForSellerEvent(OrderStatusChangedForSellerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_STATUS_CHANGED_FOR_SELLER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPreparedForCustomerEvent(OrderPreparingForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PREPARING_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPreparedForSellerEvent(OrderPreparingForSellerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_PREPARING_FOR_SELLER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreatedForCustomerEvent(OrderCreatedForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CREATED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreateFailedForCustomerEvent(OrderCreateFailedForCustomerEvent event) {
    redisTemplate.convertAndSend(RedisTopic.ORDER_CREATED_FAILED_FOR_CUSTOMER_EVENT.getTopic(), event);
  }
}
