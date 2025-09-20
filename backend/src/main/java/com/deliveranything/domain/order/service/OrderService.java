package com.deliveranything.domain.order.service;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderItemRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.repository.OrderRepository;
import com.deliveranything.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

  private final UserService userService;
  private final StoreService storeService;
  private final ProductService productService;

  private final OrderRepository orderRepository;

  @Transactional
  public OrderResponse createOrder(Long customerId, OrderCreateRequest orderCreateRequest) {
    Order order = Order.builder()
        .customer(userService.findById(customerId))
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

    return OrderResponse.from(orderRepository.save(order));
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getCustomerOrders(Long customerId) {
    return orderRepository.findAllByCustomerId(customerId)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }
}
