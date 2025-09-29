package com.deliveranything.domain.order.enums;

public enum OrderStatus {
  PENDING,
  PAID,
  PREPARING,
  RIDER_ASSIGNED,
  DELIVERING,
  COMPLETED,
  REJECTED,
  CANCELLED;

  public boolean canTransitTo(OrderStatus next) {
    return switch (this) {
      case PENDING -> next == PAID || next == CANCELLED;
      case PAID -> next == PREPARING || next == REJECTED || next == CANCELLED;
      case PREPARING -> next == RIDER_ASSIGNED;
      case RIDER_ASSIGNED -> next == DELIVERING;
      case DELIVERING -> next == COMPLETED;
      case COMPLETED, REJECTED, CANCELLED -> false;
    };
  }
}
