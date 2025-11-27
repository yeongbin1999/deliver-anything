package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.dto.OrderRiderAcceptRequest;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderRiderAcceptedEvent;
import com.deliveranything.domain.order.service.fetcher.OrderFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RiderOrderService {

  private final ApplicationEventPublisher eventPublisher;
  private final OrderFetcher orderFetcher;

  @Transactional
  public void acceptOrder(
      Long orderId,
      Long riderId,
      OrderRiderAcceptRequest orderRiderAcceptRequest
  ) {
    Order order = orderFetcher.findByIdOrThrow(orderId);
    order.updateStatus(OrderStatus.RIDER_ASSIGNED);

    eventPublisher.publishEvent(OrderRiderAcceptedEvent.fromOrderAndETA(order, riderId,
        orderRiderAcceptRequest.etaMinutes()));
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderResponse(Long orderId) {
    return OrderResponse.from(orderFetcher.findByIdOrThrow(orderId));
  }
}
