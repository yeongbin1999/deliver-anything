package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeliveryOrderService {

  private final OrderQueryService orderQueryService;

  @Transactional(readOnly = true)
  public OrderResponse getOrderByDeliveryId(Long deliveryId) {
    return OrderResponse.from(orderQueryService.findByDeliveryIdOrThrow(deliveryId));
  }

  public Order getOrderById(Long orderId) {
    return orderQueryService.findByIdOrThrow(orderId);
  }

  public Long getCustomerIdByOrderId(Long orderId) {
    return getOrderById(orderId).getCustomerProfileId();
  }

  public Long getSellerIdByOrderId(Long orderId) {
    return getOrderById(orderId).getStore().getSellerProfileId();
  }
}
