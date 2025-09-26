package com.deliveranything.domain.delivery.controller;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.service.DeliveryService;
import com.deliveranything.domain.review.dto.ReviewRatingAndListResponseDto;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.enums.MyReviewSortType;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "배달 API")
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final ReviewService reviewService;

  @PatchMapping("/status")
  @Operation(summary = "라이더 토글 전환", description = "라이더 토글 전환으로 상태를 전환합니다.")
  public ResponseEntity<ApiResponse<Void>> updateRiderStatus(
      @Valid @RequestBody RiderToggleStatusRequestDto riderStatusRequestDto
  ) {
    deliveryService.updateRiderStatus(riderStatusRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @PostMapping("/area")
  @Operation(summary = "배달 가능 지역 설정",
      description = "배달 가능 지역을 설정합니다 (현재는 1군데만, 자유로운 형식으로 가능).")
  public ResponseEntity<ApiResponse<Void>> updateDeliveryArea(
      @Valid @RequestBody DeliveryAreaRequestDto deliveryAreaRequestDto
  ) {
    deliveryService.updateDeliveryArea(deliveryAreaRequestDto);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @GetMapping("/reviews")
  @Operation(summary = "배달원 리뷰 목록 조회",
      description = "배달원에게 작성된 리뷰 목록과 함께, 전체 평점 및 별점별 개수를 조회합니다.")
  public ResponseEntity<ApiResponse<ReviewRatingAndListResponseDto>> getReviews(
      @RequestParam Long userId, // profileId 고려 -> 인증객체
      @RequestParam(required = false, defaultValue = "LATEST") MyReviewSortType sort,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "10") Integer size
  ) {
    ReviewRatingAndListResponseDto response = reviewService.getReviewRatingAndList(
        userId, sort, cursor, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/reviews/{reviewId}")
  @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<ReviewResponse>> getReviewDetail(
      @PathVariable Long reviewId
  ) {
    ReviewResponse response = reviewService.getReview(reviewId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
