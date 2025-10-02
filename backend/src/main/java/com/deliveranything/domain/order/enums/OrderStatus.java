package com.deliveranything.domain.order.enums;

public enum OrderStatus {
  CREATED,
  PAID,
  PENDING,
  PREPARING,
  RIDER_ASSIGNED,
  DELIVERING,
  COMPLETED,
  REJECTED,
  CANCELED,
  CANCELLATION_FAILED,
  PAYMENT_FAILED;

  public boolean canTransitTo(OrderStatus next) {
    return switch (this) {
      case CREATED -> next == PAID || next == PAYMENT_FAILED;
      case PAID -> next == PENDING || next == CANCELED || next == CANCELLATION_FAILED;
      case PENDING ->
          next == PREPARING || next == REJECTED || next == CANCELED || next == CANCELLATION_FAILED;
      case PREPARING -> next == RIDER_ASSIGNED;
      case RIDER_ASSIGNED -> next == DELIVERING;
      case DELIVERING -> next == COMPLETED;
      case COMPLETED, REJECTED, CANCELED, CANCELLATION_FAILED, PAYMENT_FAILED -> false;
    };
  }
}
