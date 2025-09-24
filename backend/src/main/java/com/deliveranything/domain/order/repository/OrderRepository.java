package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);

  Optional<Order> findByMerchantId(String merchantId);

  Optional<Order> findByDeliveryId(Long deliveryId);

  List<Order> findByStatus(OrderStatus orderStatus);

  List<Order> findByDeliveryRiderProfileId(Long riderProfileId);
}
