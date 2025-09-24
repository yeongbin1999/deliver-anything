package com.deliveranything.domain.order.enums;

public enum OrderStatus {
  PENDING,
  PREPARING,
  RIDER_ASSIGNED,
  DELIVERING,
  COMPLETED,
  CANCELLED;

  public boolean canTransitTo(OrderStatus next) {
    return switch (this) {
      case PENDING -> next == PREPARING || next == CANCELLED;
      case PREPARING -> next == RIDER_ASSIGNED;
      case RIDER_ASSIGNED -> next == DELIVERING;
      case DELIVERING -> next == COMPLETED;
      case COMPLETED, CANCELLED -> false;
    };
  }
}
