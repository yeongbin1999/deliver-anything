package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);

  Optional<Order> findByMerchantId(String merchantId);

  @Query("SELECT o FROM Order o JOIN FETCH o.store WHERE o.delivery.id = :deliveryId")
  Optional<Order> findByDeliveryIdWithStore(Long deliveryId);

  @Query("SELECT o FROM Order o JOIN FETCH o.store WHERE o.status = :status")
  List<Order> findAllByStatusWithStore(OrderStatus status);

  @Query("SELECT o FROM Order o JOIN FETCH o.store WHERE o.delivery.riderProfile.id = :riderProfileId")
  List<Order> findAllByDeliveryRiderProfileIdWithStore(Long riderProfileId);

  List<Order> findAllByStoreIdAndStatusInOrderByCreatedAtAsc(Long storeId,
      List<OrderStatus> statuses);
}
