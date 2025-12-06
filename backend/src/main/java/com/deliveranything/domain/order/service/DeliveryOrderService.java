package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
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
}
