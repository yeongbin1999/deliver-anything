package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderCreateResponse;
import com.deliveranything.domain.order.dto.OrderItemRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.order.repository.OrderRepositoryCustom;
import com.deliveranything.domain.product.product.service.ProductService;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.service.CustomerProfileService;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
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
public class CustomerOrderService {

  private final CustomerProfileService customerProfileService;
  private final ProductService productService;
  private final StoreService storeService;

  private final OrderRepository orderRepository;
  private final OrderRepositoryCustom orderRepositoryCustom;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public OrderCreateResponse createOrder(Long customerId, OrderCreateRequest orderCreateRequest) {
    CustomerProfile customerProfile = customerProfileService.getProfileByProfileId(customerId);
    Store store = storeService.getStoreById(orderCreateRequest.storeId());

    Order order = orderCreateRequest.toEntity(customerProfile, store);
    for (OrderItemRequest orderItemRequest : orderCreateRequest.orderItemRequests()) {
      OrderItem orderItem = OrderItem.builder()
          .product(productService.getProductById(orderItemRequest.productId()))
          .price(orderItemRequest.price())
          .quantity(orderItemRequest.quantity())
          .build();

      order.addOrderItem(orderItem);
    }

    Order savedOrder = orderRepository.save(order);
    eventPublisher.publishEvent(OrderCreatedEvent.from(savedOrder));

    return OrderCreateResponse.from(savedOrder);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getCustomerOrdersByCursor(
      Long customerId,
      String nextPageToken,
      int size
  ) {
    return getOrdersByCursorInternal(customerId, nextPageToken, size, Collections.emptyList());
  }

  @Transactional(readOnly = true)
  public OrderResponse getCustomerOrder(Long orderId, Long customerId) {
    return OrderResponse.from(
        orderRepository.findOrderWithStoreByIdAndCustomerId(orderId, customerId)
            .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_ORDER_NOT_FOUND)));
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getProgressingOrders(Long customerId) {
    return orderRepository.findOrdersWithStoreByCustomerIdAndStatuses(customerId, List.of(
            OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.RIDER_ASSIGNED,
            OrderStatus.DELIVERING)).stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getCompletedOrdersByCursor(
      Long customerId,
      String nextPageToken,
      int size
  ) {
    return getOrdersByCursorInternal(customerId, nextPageToken, size,
        List.of(OrderStatus.COMPLETED));
  }

  private CursorPageResponse<OrderResponse> getOrdersByCursorInternal(
      Long customerId,
      String nextPageToken,
      int size,
      List<OrderStatus> statuses
  ) {
    LocalDateTime lastCreatedAt = null;
    Long lastOrderId = null;
    Object[] decodedParts = CursorUtil.decode(nextPageToken);

    if (decodedParts != null && decodedParts.length == 2) {
      try {
        lastCreatedAt = LocalDateTime.parse(decodedParts[0].toString());
        lastOrderId = Long.parseLong(decodedParts[1].toString());
      } catch (DateTimeParseException e) {
        log.warn("커서 토큰에서 날짜 파싱 실패", e);
      } catch (NumberFormatException e) {
        log.warn("커서 토큰에서 주문ID 파싱 실패", e);
      }
    }

    List<Order> orders;
    if (statuses == null || statuses.isEmpty()) {
      orders = orderRepositoryCustom.findOrdersWithStoreByCustomerId(customerId,
          lastCreatedAt, lastOrderId, size + 1);
    } else {
      orders = orderRepositoryCustom.findOrdersWithStoreByCustomerId(customerId,
          statuses, lastCreatedAt, lastOrderId, size + 1);
    }

    List<OrderResponse> orderResponses = orders.stream()
        .limit(size)
        .map(OrderResponse::from)
        .toList();

    boolean hasNext = orders.size() > size;

    try {
      OrderResponse lastResponse = orderResponses.getLast();
      return new CursorPageResponse<>(
          orderResponses,
          hasNext ? CursorUtil.encode(lastResponse.createdAt(), lastResponse.id()) : null,
          hasNext
      );
    } catch (NoSuchElementException e) {
      return new CursorPageResponse<>(orderResponses, null, false);
    }
  }
}
