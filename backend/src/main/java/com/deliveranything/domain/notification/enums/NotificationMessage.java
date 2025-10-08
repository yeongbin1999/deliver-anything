package com.deliveranything.domain.notification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationMessage {

  ORDER_PAID_CUSTOMER("주문 결제가 성공적으로 완료되었습니다."),
  ORDER_PAID_SELLER("새로운 주문 요청이 있습니다.");

  private final String message;
}
