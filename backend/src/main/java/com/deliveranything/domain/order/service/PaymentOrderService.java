package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentOrderService {

  private final PaymentService paymentService;

  private final OrderRepository orderRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public OrderResponse payOrder(String merchantUid, String paymentKey) {
    Order order = getOrderByMerchantId(merchantUid);
    order.isPayable();

    eventPublisher.publishEvent(
        new OrderPaymentRequestedEvent(paymentKey, merchantUid, order.getTotalPrice().longValue()));

    //?
    try {
      paymentService.confirmPayment(paymentKey, merchantUid, order.getTotalPrice().longValue());
      order.updateStatus(OrderStatus.PENDING);
    } catch (CustomException e) {
      order.updateStatus(OrderStatus.PAYMENT_FAILED);
      throw e;
    }

    return OrderResponse.from(order);
  }

  @Transactional
  public OrderResponse cancelOrder(Long orderId, String cancelReason) {
    Order order = getOrderWithStoreById(orderId);

    try {
      order.updateStatus(OrderStatus.CANCELLATION_REQUESTED);
      paymentService.cancelPayment(order.getMerchantId(), cancelReason);
      order.updateStatus(OrderStatus.CANCELED);
    } catch (CustomException e) {
      order.updateStatus(OrderStatus.PENDING);
      throw e;
    }

    return OrderResponse.from(order);
  }

  private Order getOrderByMerchantId(String merchantId) {
    return orderRepository.findOrderWithStoreByMerchantId(merchantId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  private Order getOrderWithStoreById(Long orderId) {
    return orderRepository.findOrderWithStoreById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }
}
