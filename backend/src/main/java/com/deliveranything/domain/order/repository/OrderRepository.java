package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

  Optional<Order> findByMerchantId(String merchantId);

  Optional<Order> findByIdAndCustomerProfileId(Long orderId, Long customerProfileId);

  Optional<Order> findByDeliveryId(Long deliveryId);

  List<Order> findByStoreIdAndStatus(Long storeId, OrderStatus status);

  List<Order> findByStoreIdAndStatusInOrderByCreatedAtAsc(
      Long storeId,
      List<OrderStatus> statuses
  );

  List<Order> findByCustomerProfileIdAndStatusInOrderByCreatedAtDesc(
      Long customerProfileId,
      List<OrderStatus> statuses
  );
}
