package com.deliveranything.domain.order.service.fetcher;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderFetcher {

  private final OrderRepository orderRepository;

  public Order findByIdOrThrow(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  public Order findByMerchantIdOrThrow(String merchantId) {
    return orderRepository.findByMerchantId(merchantId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  public Order findByIdAndCustomerProfileIdOrThrow(Long orderId, Long customerProfileId) {
    return orderRepository.findByIdAndCustomerProfileId(orderId, customerProfileId)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_ORDER_NOT_FOUND));
  }

  public Order findByDeliveryIdOrThrow(Long deliveryId) {
    return orderRepository.findByDeliveryId(deliveryId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }
}
