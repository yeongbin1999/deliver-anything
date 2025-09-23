package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderCreateResponse;
import com.deliveranything.domain.order.dto.OrderItemRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.order.repository.OrderRepositoryCustom;
import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.domain.user.service.CustomerProfileService;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final StoreService storeService;
  private final CustomerProfileService customerProfileService;
  private final PaymentService paymentService;
  private final ProductService productService;

  private final OrderRepository orderRepository;
  private final OrderRepositoryCustom orderRepositoryCustom;

  @Transactional
  public OrderCreateResponse createOrder(Long customerId, OrderCreateRequest orderCreateRequest) {
    Order order = Order.builder()
        .customer(customerProfileService.getProfile(customerId))
        .store(storeService.getStore(orderCreateRequest.storeId()))
        .address(orderCreateRequest.address())
        .riderNote(orderCreateRequest.riderNote())
        .storeNote(orderCreateRequest.storeNote())
        .totalPrice(orderCreateRequest.totalPrice())
        .storePrice(orderCreateRequest.storePrice())
        .deliveryPrice(orderCreateRequest.deliveryPrice())
        .build();

    for (OrderItemRequest orderItemRequest : orderCreateRequest.orderItemRequests()) {
      OrderItem orderItem = OrderItem.builder()
          .product(productService.getProduct(orderItemRequest.productId()))
          .price(orderItemRequest.price())
          .quantity(orderItemRequest.quantity())
          .build();

      order.addOrderItem(orderItem);
    }

    Order savedOrder = orderRepository.save(order);

    return OrderCreateResponse.of(savedOrder, paymentService.createPayment(savedOrder.getId(),
        orderCreateRequest.totalPrice()));
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<OrderResponse> getCustomerOrders(
      Long customerId,
      Long cursor,
      int size
  ) {
    List<Order> orders = orderRepositoryCustom.findCustomerOrders(customerId, cursor, size + 1);
    List<OrderResponse> orderResponses = orders.stream()
        .limit(size)
        .map(OrderResponse::from)
        .toList();

    boolean hasNext = orders.size() > size;

    return new CursorPageResponse<>(
        orderResponses,
        hasNext ? orderResponses.getLast().orderId().toString() : null,
        hasNext
    );
  }

  @Transactional(readOnly = true)
  public OrderResponse getCustomerOrder(Long orderId, Long customerId) {
    return OrderResponse.from(orderRepository.findByIdAndCustomerId(orderId, customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_ORDER_NOT_FOUND)));
  }

  public Order getOrder(String merchantId) {
    return orderRepository.findByMerchantId(merchantId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }
}
