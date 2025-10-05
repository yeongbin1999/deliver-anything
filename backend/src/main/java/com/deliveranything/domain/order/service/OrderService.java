package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final OrderRepository orderRepository;

  @Transactional
  public void processPaymentCompletion(String merchantUid) {
    Order order = getOrder(merchantUid);
    order.updateStatus(OrderStatus.PENDING);
    // TODO: SSE 새 주문이 생성됐다고 상점에 전달
    // TODO: SSE 결제 성공했다고 소비자에게 전달
  }

  @Transactional
  public void processPaymentFailure(String merchantUid) {
    Order order = getOrder(merchantUid);
    order.updateStatus(OrderStatus.PAYMENT_FAILED);
    // TODO: SSE 결제 실패했다고 소비자에게 알림
  }

  private Order getOrder(String merchantUid) {
    return orderRepository.findByMerchantId(merchantUid).orElseThrow(() -> new CustomException(
        ErrorCode.ORDER_NOT_FOUND));
  }
}
