package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findAllByCustomerId(Long customerId);
}
