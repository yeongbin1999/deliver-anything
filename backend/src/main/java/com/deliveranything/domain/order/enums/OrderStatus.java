package com.deliveranything.domain.order.enums;

import java.util.List;

public enum OrderStatus {
  CREATED,
  PENDING,
  PREPARING,
  RIDER_ASSIGNED,
  DELIVERING,
  COMPLETED,
  REJECTED,
  CANCELED,
  CANCELLATION_REQUESTED,
  CANCEL_FAILED,
  PAYMENT_FAILED;

  public static final List<OrderStatus> CUSTOMER_ORDER_IN_PROGRESS_STATUSES = List.of(
      PENDING, PREPARING, RIDER_ASSIGNED, DELIVERING
  );

  public static final List<OrderStatus> STORE_ORDER_IN_PROGRESS_STATUSES = List.of(
      PREPARING, RIDER_ASSIGNED, DELIVERING
  );

  public static final List<OrderStatus> STORE_ORDER_FINALIZED_STATUSES = List.of(
      COMPLETED, REJECTED
  );

  public static final List<OrderStatus> COMPLETED_STATUSES = List.of(COMPLETED);

  public boolean canTransitTo(OrderStatus next) {
    return switch (this) {
      case CREATED -> next == PENDING || next == PAYMENT_FAILED || next == CANCELED;
      case PENDING -> next == PREPARING || next == REJECTED || next == CANCELED
          || next == CANCELLATION_REQUESTED;
      case PREPARING -> next == RIDER_ASSIGNED;
      case RIDER_ASSIGNED -> next == DELIVERING;
      case DELIVERING -> next == COMPLETED;
      case CANCELLATION_REQUESTED -> next == PENDING || next == CANCEL_FAILED;
      case COMPLETED, REJECTED, CANCELED, CANCEL_FAILED, PAYMENT_FAILED -> false;
    };
  }
}
