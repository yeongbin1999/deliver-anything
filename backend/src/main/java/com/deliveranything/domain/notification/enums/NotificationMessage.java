package com.deliveranything.domain.notification.enums;

import com.deliveranything.domain.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationMessage {

  ORDER_PAID_CUSTOMER("주문 결제가 성공적으로 완료되었습니다."),
  ORDER_PAID_SELLER("새로운 주문 요청이 있습니다."),
  ORDER_PAYMENT_FAILED_CUSTOMER("결제에 실패했습니다. 다시 시도해주세요."),
  ORDER_CANCELED_CUSTOMER("주문이 성공적으로 취소되었습니다."),
  ORDER_CANCELED_SELLER("주문이 정상적으로 거절되었습니다.");

  private final String message;

  public static String getMessageByOrderStatus(OrderStatus orderStatus) {
    return switch (orderStatus) {
      case OrderStatus.RIDER_ASSIGNED -> "배달원이 배정됐습니다.";
      case OrderStatus.DELIVERING -> "음식이 배달중입니다.";
      case OrderStatus.COMPLETED -> "배달이 완료되었습니다.";
      default -> "정의하지 않은 SSE 주문 상태 알림입니다. -> " + orderStatus;
    };
  }
}
