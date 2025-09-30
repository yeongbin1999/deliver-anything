package com.deliveranything.domain.order.enums;

public enum OrderStatus {
  PENDING,
  PAID,
  PREPARING,
  RIDER_ASSIGNED,
  DELIVERING,
  COMPLETED,
  REJECTED,
  CANCELED,
  PAYMENT_FAILED;

  public boolean canTransitTo(OrderStatus next) {
    return switch (this) {
      case PENDING -> next == PAID || next == CANCELED || next == PAYMENT_FAILED;
      case PAID -> next == PREPARING || next == REJECTED || next == CANCELED;
      case PREPARING -> next == RIDER_ASSIGNED;
      case RIDER_ASSIGNED -> next == DELIVERING;
      case DELIVERING -> next == COMPLETED;
      case COMPLETED, REJECTED, CANCELED, PAYMENT_FAILED -> false;
    };
  }
}
