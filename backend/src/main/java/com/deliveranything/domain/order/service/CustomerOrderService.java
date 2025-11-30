package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderCreateResponse;
import com.deliveranything.domain.order.dto.OrderCursor;
import com.deliveranything.domain.order.dto.OrderItemRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.product.product.service.ProductService;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.service.CustomerProfileService;
import com.deliveranything.global.common.CursorFactory;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.util.CursorUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

  private final ApplicationEventPublisher eventPublisher;
  private final OrderQueryService orderQueryService;
  private final OrderRepository orderRepository;

  @Transactional
  public OrderCreateResponse createOrder(
      Long customerProfileId,
      OrderCreateRequest orderCreateRequest
  ) {
    Store store = storeService.getStoreById(orderCreateRequest.storeId());
    CustomerProfile customerProfile = customerProfileService.getProfileByProfileId(
        customerProfileId);

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
      Long customerProfileId,
      String nextPageToken,
      long size
  ) {
    return getOrdersByCursorInternal(customerProfileId, nextPageToken, size,
        Collections.emptyList());
  }

  @Transactional(readOnly = true)
  public OrderResponse getCustomerOrder(Long orderId, Long customerProfileId) {
    return OrderResponse.from(
        orderQueryService.findByIdAndCustomerProfileIdOrThrow(orderId, customerProfileId));
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getProgressingOrders(Long customerProfileId) {
    return orderRepository.findByCustomerProfileIdAndStatusInOrderByCreatedAtDesc(
            customerProfileId, OrderStatus.CUSTOMER_ORDER_IN_PROGRESS_STATUSES).stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getCompletedOrdersByCursor(
      Long customerProfileId,
      String nextPageToken,
      long size
  ) {
    return getOrdersByCursorInternal(customerProfileId, nextPageToken, size,
        OrderStatus.COMPLETED_STATUSES);
  }

  private CursorPageResponse<OrderResponse> getOrdersByCursorInternal(
      Long customerProfileId,
      String nextPageToken,
      long size,
      List<OrderStatus> statuses
  ) {
    OrderCursor cursor = OrderCursor.fromToken(nextPageToken);

    List<Order> orders = orderRepository.findOrdersByCustomerProfileIdWithCursor(
        customerProfileId, statuses, cursor.createdAt(), cursor.orderId(), size + 1L);

    return CursorFactory.create(orders, size, OrderResponse::from, OrderCursor::from);
  }
}