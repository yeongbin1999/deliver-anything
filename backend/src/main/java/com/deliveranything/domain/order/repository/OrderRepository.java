package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

  Optional<Order> findByMerchantId(String merchantId);

  Optional<Order> findByIdAndCustomerProfileId(Long orderId, Long customerProfileId);

  Optional<Order> findByDeliveryId(Long deliveryId);

  default Order findByIdOrThrow(Long orderId) {
    return findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  default Order findByMerchantIdOrThrow(String merchantId) {
    return findByMerchantId(merchantId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  default Order findByIdAndCustomerProfileIdOrThrow(Long orderId, Long customerProfileId) {
    return findByIdAndCustomerProfileId(orderId, customerProfileId)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_ORDER_NOT_FOUND));
  }

  default Order findByDeliveryIdOrThrow(Long deliveryId) {
    return findByDeliveryId(deliveryId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

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
