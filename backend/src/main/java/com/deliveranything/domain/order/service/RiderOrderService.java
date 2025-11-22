package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.dto.OrderRiderAcceptRequest;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderRiderAcceptedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RiderOrderService {

  private final OrderRepository orderRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void acceptOrder(
      Long orderId,
      Long riderId,
      OrderRiderAcceptRequest orderRiderAcceptRequest
  ) {
    Order order = getOrder(orderId);
    order.updateStatus(OrderStatus.RIDER_ASSIGNED);

    eventPublisher.publishEvent(OrderRiderAcceptedEvent.fromOrderAndETA(order, riderId,
        orderRiderAcceptRequest.etaMinutes()));
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderResponse(Long orderId) {
    return OrderResponse.from(orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND)));
  }

  @Transactional(readOnly = true)
  public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }
}
