package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.enums.Publisher;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import com.deliveranything.domain.order.event.OrderStoreAcceptedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.util.CursorUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreOrderService {

  private final ApplicationEventPublisher eventPublisher;
  private final OrderQueryService orderQueryService;
  private final OrderRepository orderRepository;

  // 주문 이력 조회
  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getStoreOrdersByCursor(
      Long storeId,
      String nextPageToken,
      int size
  ) {
    LocalDateTime lastCreatedAt = null;
    Long lastOrderId = null;
    Object[] decodedParts = CursorUtil.decode(nextPageToken);

    if (decodedParts != null && decodedParts.length == 2) {
      try {
        lastCreatedAt = LocalDateTime.parse(decodedParts[0].toString());
        lastOrderId = Long.parseLong(decodedParts[1].toString());
      } catch (NumberFormatException e) {
        lastCreatedAt = null;
        lastOrderId = null;
      }
    }

    List<Order> cursorOrders = orderRepository.findOrdersByStoreIdWithCursor(storeId,
        List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED), lastCreatedAt, lastOrderId, size + 1);

    List<OrderResponse> cursorResponses = cursorOrders.stream()
        .limit(size)
        .map(OrderResponse::from)
        .toList();

    boolean hasNext = cursorOrders.size() > size;

    try {
      OrderResponse lastResponse = cursorResponses.getLast();
      return new CursorPageResponse<>(
          cursorResponses,
          hasNext ? CursorUtil.encode(lastResponse.createdAt(), lastResponse.id()) : null,
          hasNext
      );
    } catch (NoSuchElementException e) {
      return new CursorPageResponse<>(cursorResponses, null, hasNext);
    }
  }

  // 들어온 주문 중 수락 or 거절 해야하는 목록 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getPendingOrders(Long storeId) {
    return orderRepository.findByStoreIdAndStatus(storeId, OrderStatus.PENDING)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getProgressingOrders(Long storeId) {
    return orderRepository.findByStoreIdAndStatusInOrderByCreatedAtAsc(storeId,
            OrderStatus.STORE_ORDER_IN_PROGRESS_STATUSES)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional
  public void acceptOrder(Long orderId) {
    Order order = orderQueryService.findByIdOrThrow(orderId);

    log.info("상점이 주문 수락 했을 때 도착지의 latitude 위도 -90~90: {} / longitude 경도 -180~180: {}",
        order.getDestination().getY(), order.getDestination().getX());
    log.info("상점이 주문 수락 했을 때 상점의 latitude 위도 -90~90: {} / longitude 경도 -180~180: {}",
        order.getStore().getLocation().getY(), order.getStore().getLocation().getX());

    eventPublisher.publishEvent(OrderStoreAcceptedEvent.from(order));
  }

  @Transactional
  public void rejectOrder(Long orderId) {
    String STORE_CANCEL_REASON = "상점이 주문을 거절했습니다.";

    Order order = orderQueryService.findByIdOrThrow(orderId);
    order.cancellationRequest(STORE_CANCEL_REASON);

    eventPublisher.publishEvent(
        OrderRejectedEvent.from(order, STORE_CANCEL_REASON, Publisher.STORE));
  }
}