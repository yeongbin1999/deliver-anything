package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.Publisher;
import com.deliveranything.domain.order.event.OrderCancelEvent;
import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.order.service.fetcher.OrderFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentOrderService {

  private final ApplicationEventPublisher eventPublisher;
  private final OrderFetcher orderFetcher;

  @Transactional
  public void payOrder(String merchantUid, String paymentKey) {
    log.info("고객의 결제 시도 - 주문 번호: {} & PG 사 결제 키: {}", merchantUid, paymentKey);

    Order order = orderFetcher.findByMerchantIdOrThrow(merchantUid);
    order.isPayable();

    eventPublisher.publishEvent(
        OrderPaymentRequestedEvent.fromOrderAndPaymentKey(order, paymentKey));
  }

  @Transactional
  public void cancelOrder(Long orderId, String cancelReason) {
    Order order = orderFetcher.findByIdOrThrow(orderId);
    order.cancellationRequest(cancelReason);

    eventPublisher.publishEvent(OrderCancelEvent.from(order, cancelReason, Publisher.CUSTOMER));
  }
}
