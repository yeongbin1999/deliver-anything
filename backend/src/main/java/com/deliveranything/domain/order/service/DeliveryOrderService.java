package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.service.fetcher.OrderFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeliveryOrderService {

  private final OrderFetcher orderFetcher;

  @Transactional(readOnly = true)
  public OrderResponse getOrderByDeliveryId(Long deliveryId) {
    return OrderResponse.from(orderFetcher.findByDeliveryIdOrThrow(deliveryId));
  }

  public Order getOrderById(Long orderId) {
    return orderFetcher.findByIdOrThrow(orderId);
  }

  public Long getCustomerIdByOrderId(Long orderId) {
    return getOrderById(orderId).getCustomerProfileId();
  }

  public Long getSellerIdByOrderId(Long orderId) {
    return getOrderById(orderId).getStore().getSellerProfileId();
  }
}
