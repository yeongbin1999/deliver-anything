package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.enums.Publisher;
import com.deliveranything.domain.order.event.OrderCompletedEvent;
import com.deliveranything.domain.order.event.sse.OrderCanceledForCustomerEvent;
import com.deliveranything.domain.order.event.sse.OrderCanceledForSellerEvent;
import com.deliveranything.domain.order.event.sse.OrderPaidForCustomerEvent;
import com.deliveranything.domain.order.event.sse.OrderPaidForSellerEvent;
import com.deliveranything.domain.order.event.sse.OrderPaymentFailedForCustomerEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final ApplicationEventPublisher eventPublisher;
  private final OrderRepository orderRepository;

  @Transactional
  public void processPaymentCompletion(String merchantUid) {
    Order order = getOrderWithStoreByMerchantId(merchantUid);
    order.updateStatus(OrderStatus.PENDING);

    eventPublisher.publishEvent(OrderPaidForCustomerEvent.fromOrder(order));
    eventPublisher.publishEvent(OrderPaidForSellerEvent.fromOrder(order));
  }

  @Transactional
  public void processPaymentFailure(String merchantUid) {
    Order order = getOrderByMerchantId(merchantUid);
    order.updateStatus(OrderStatus.PAYMENT_FAILED);

    eventPublisher.publishEvent(OrderPaymentFailedForCustomerEvent.fromOrder(order));
  }

  @Transactional
  public void processPaymentCancelSuccess(String merchantUid, Publisher publisher) {
    Order order = getOrderByMerchantId(merchantUid);
    if (publisher == Publisher.CUSTOMER) {
      order.updateStatus(OrderStatus.CANCELED);
    } else if (publisher == Publisher.STORE) {
      order.updateStatus(OrderStatus.REJECTED);
    }

    eventPublisher.publishEvent(new OrderCanceledForCustomerEvent());
    eventPublisher.publishEvent(OrderCanceledForSellerEvent.fromOrder(order));
  }

  @Transactional
  public void processDeliveryRiderAssigned(Long orderId) {
    Order order = getOrderById(orderId);
    order.updateStatus(OrderStatus.RIDER_ASSIGNED);
    // TODO: SSE 상점의 주문 현황에 준비중이던거 배정 완료로 업데이트 하라고 전달
  }

  @Transactional
  public void processDeliveryPickedUp(Long orderId) {
    Order order = getOrderById(orderId);
    order.updateStatus(OrderStatus.DELIVERING);
    // TODO: SSE 상점의 주문 현황에 배정 완료이던거 배달 중으로 업데이트 하라고 전달
  }

  @Transactional
  public void processDeliveryCompleted(Long orderId, Long riderProfileId, Long sellerProfileId) {
    Order order = getOrderById(orderId);
    order.updateStatus(OrderStatus.COMPLETED);

    eventPublisher.publishEvent(new OrderCompletedEvent(orderId, riderProfileId, sellerProfileId,
        order.getStorePrice(), order.getDeliveryPrice()));
    // TODO: SSE 상점의 주문 현황에 배정 중이던거 제거하라고 전달
  }

  private Order getOrderWithStoreByMerchantId(String merchantUid) {
    return orderRepository.findOrderWithStoreByMerchantId(merchantUid)
        .orElseThrow(() -> new CustomException(
            ErrorCode.ORDER_NOT_FOUND));
  }

  private Order getOrderByMerchantId(String merchantUid) {
    return orderRepository.findByMerchantId(merchantUid).orElseThrow(() -> new CustomException(
        ErrorCode.ORDER_NOT_FOUND));
  }

  private Order getOrderById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new CustomException(
        ErrorCode.ORDER_NOT_FOUND));
  }
}
