package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);
}
