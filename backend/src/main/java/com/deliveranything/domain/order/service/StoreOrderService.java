package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderAcceptedEvent;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.order.repository.OrderRepositoryCustom;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StoreOrderService {

  private final OrderRepository orderRepository;
  private final OrderRepositoryCustom orderRepositoryCustom;

  private final ApplicationEventPublisher eventPublisher;

  // 주문 이력 조회
  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getStoreOrdersByCursor(
      Long storeId,
      String nextPageToken,
      int size
  ) {
    LocalDateTime lastCreatedAt = null;
    Long lastOrderId = null;
    String[] decodedParts = CursorUtil.decode(nextPageToken);

    if (decodedParts != null && decodedParts.length == 2) {
      try {
        lastCreatedAt = LocalDateTime.parse(decodedParts[0]);
        lastOrderId = Long.parseLong(decodedParts[1]);
      } catch (NumberFormatException e) {
        lastCreatedAt = null;
        lastOrderId = null;
      }
    }

    List<Order> cursorOrders = orderRepositoryCustom.findOrdersWithStoreByStoreId(storeId,
        List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED), lastCreatedAt, lastOrderId, size + 1);

    List<OrderResponse> cursorResponses = cursorOrders.stream()
        .limit(size)
        .map(OrderResponse::from)
        .toList();

    boolean hasNext = cursorOrders.size() > size;
    OrderResponse lastResponse = cursorResponses.getLast();

    return new CursorPageResponse<>(
        cursorResponses,
        hasNext ? CursorUtil.encode(lastResponse.createdAt(), lastResponse.id()) : null,
        hasNext
    );
  }

  // 들어온 주문 중 수락 or 거절 해야하는 목록 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getPendingOrders(Long storeId) {
    return orderRepository.findOrdersWithStoreByStoreIdAndStatus(storeId, OrderStatus.PENDING)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }

  // 주문 현황 목록 조회
  @Transactional(readOnly = true)
  public List<OrderResponse> getAcceptedOrders(Long storeId) {
    return orderRepository.findOrdersWithStoreByStoreIdAndStatuses(storeId,
            List.of(OrderStatus.PREPARING, OrderStatus.RIDER_ASSIGNED, OrderStatus.DELIVERING)).stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional
  public OrderResponse acceptOrder(Long orderId) {
    Order order = getOrderWithStore(orderId);
    order.updateStatus(OrderStatus.PREPARING);
    //TODO: SSE가 이거 구독해서 주문 수락화면에 현 리스트에서 주문 제거하라고 전달해야함.
    eventPublisher.publishEvent(OrderAcceptedEvent.from(order));

    return OrderResponse.from(order);
  }

  @Transactional
  public OrderResponse rejectOrder(Long orderId) {
    Order order = getOrderWithStore(orderId);
    order.updateStatus(OrderStatus.CANCELLATION_REQUESTED);
//이후 reject or 취소 실패는 3일후 환불 처리
    eventPublisher.publishEvent(OrderRejectedEvent.from(order, "상점이 주문을 거절했습니다."));
    // TODO: SSE 알림을 통해 상점에서 거절한 주문 제거하라고 전달
    return OrderResponse.from(order);
  }

  private Order getOrderWithStore(Long orderId) {
    return orderRepository.findOrderWithStoreById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }
}
