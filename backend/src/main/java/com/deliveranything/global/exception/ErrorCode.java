package com.deliveranything.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/***
 * 해당 커스텀 예외 코드처럼 패키지별로 작업하시면서 MemberErrorCode 이런 식으로 작성하시면 됩니다.
 * HttpStatus와 String code는 상황에 맞게 사용하시면 됩니다.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 유저 관련 오류
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "사용자를 찾을 수 없습니다."),

  // 배달/라이더 관련 오류
  RIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "RIDER-404", "라이더를 찾을 수 없습니다."),

  // 리뷰 관련 오류
  REVIEW_NO_PERMISSION(HttpStatus.FORBIDDEN, "REVIEW-403", "리뷰를 관리할 권한이 없습니다."),
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-404", "리뷰를 찾을 수 없습니다."),

  // 주문 관련 오류
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-404", "주문 정보를 찾을 수 없습니다."),
  CUSTOMER_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-CUSTOMER-404", "소비자의 주문을 찾을 수 없습니다."),

  // 결제 관련 오류
  PG_PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "PG-PAYMENT-001",
      "결제 검증 실패: 결제 ID, 주문 번호, 결제 금액 중 하나 이상 불일치합니다."),
  PG_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PG-PAYMENT-404", "PG사에서 주문의 결제 이력을 찾을 수 없습니다."),


  PAYMENT_AMOUNT_NOT_VALID(HttpStatus.BAD_REQUEST, "PAYMENT-001", "결제 금액이 주문의 가격과 다릅니다."),
  PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-404", "결제 이력을 찾을 수 없습니다."),
  PAYMENT_INVALID_STATUS(HttpStatus.CONFLICT, "PAYMENT-409", "결제 상태를 변경할 수 없습니다."),

  // 상점 관련 오류
  STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-404", "상점을 찾을 수 없습니다."),
  STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-CATEGORY-404", "상점 카테고리를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}