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

  // 인가 관련 오류
  PROFILE_TYPE_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-403", "해당 프로필 유형으로는 접근할 수 없습니다."),

  // 배달/라이더 관련 오류
  RIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "RIDER-404", "라이더를 찾을 수 없습니다."),
  DELIVERY_NOT_AVAILABLE_IN_PROGRESS(HttpStatus.CONFLICT, "DELIVERY-409",
      "현재 배달 진행 중이므로 해당 작업을 수행할 수 없습니다."),
  DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-404", "배달을 찾을 수 없습니다."),

  // 리뷰 관련 오류
  REVIEW_NO_PERMISSION(HttpStatus.FORBIDDEN, "REVIEW-403", "리뷰를 관리할 권한이 없습니다."),
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-404", "리뷰를 찾을 수 없습니다."),
  REVIEW_ALREADY_LIKED(HttpStatus.CONFLICT, "REVIEW-001", "리뷰에 이미 좋아요를 눌렀습니다."),
  REVIEW_NOT_LIKED(HttpStatus.CONFLICT, "REVIEW-002", "좋아요를 누르지 않은 리뷰입니다."),
  REVIEW_INVALID_TARGET(HttpStatus.BAD_REQUEST, "REVIEW-003", "리뷰 대상이 유효하지 않습니다."),

  // 주문 관련 오류
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-404", "주문 정보를 찾을 수 없습니다."),
  CUSTOMER_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-CUSTOMER-404", "소비자의 주문을 찾을 수 없습니다."),
  ORDER_PAY_STATUS_UNAVAILABLE(HttpStatus.CONFLICT, "ORDER-409", "결제 대기 중인 주문이 아닙니다."),

  // 결제 관련 오류
  PG_PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "PG-PAYMENT-001",
      "결제 검증 실패: 결제 ID, 주문 번호, 결제 금액 중 하나 이상 불일치합니다."),
  PG_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PG-PAYMENT-404", "PG사에서 주문의 결제 이력을 찾을 수 없습니다."),

  PAYMENT_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT-001", "결제 금액이 주문의 가격과 다릅니다."),
  PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-404", "결제 이력을 찾을 수 없습니다."),
  PAYMENT_INVALID_STATUS(HttpStatus.CONFLICT, "PAYMENT-409", "결제 상태를 변경할 수 없습니다."),

  // 상점 관련 오류
  STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-404", "상점을 찾을 수 없습니다."),
  STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-CATEGORY-404", "상점 카테고리를 찾을 수 없습니다."),

  // 상품 관련 오류
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-404", "상품을 찾을 수 없습니다."),
  PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT-400", "상품의 재고가 부족합니다."),

  // SSE 관련 오류
  SSE_SUBSCRIBE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SSE-503", "SSE 연결에 실패했습니다."),

  // Redis 관련 오류
  REDIS_MESSAGE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS-500",
      "Redis 메시지 처리 중 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}