package com.deliveranything.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// 해당 커스텀 예외 코드처럼 패키지별로 작업하시면서 MemberErrorCode 이런 식으로 작성하시면 됩니다.
// httpStatus와 String code는 상황에 맞게 사용하시면 됩니다.
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 커스텀 처리할 오류
    DEV_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "사용자를 찾을 수 없습니다(커스텀 예외 처리)"),

    //배달/라이더 관련 오류
    RIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "RIDER-404", "라이더를 찾을 수 없습니다"),

    //리뷰 관련 오류
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-404", "리뷰를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}