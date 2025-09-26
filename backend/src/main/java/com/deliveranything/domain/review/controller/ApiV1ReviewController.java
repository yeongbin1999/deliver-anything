package com.deliveranything.domain.review.controller;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewLikeResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.dto.ReviewUpdateRequest;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reviews")
@Tag(name = "Review", description = "리뷰 관련 API")
public class ApiV1ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  @Operation(summary = "리뷰 생성", description = "새로운 리뷰를 생성합니다.")
  public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
      @RequestBody ReviewCreateRequest request
//      , @AuthenticationPrincipal SecurityUser user
      //todo: 인증객체 받아와서 createReview에 전달
  ) {
    Long userId = 1L; //임시 유저 id
    ReviewCreateResponse response = reviewService.createReview(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
  }

  @DeleteMapping("/{reviewId}")
  @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
  public ResponseEntity<ApiResponse<String>> deleteReview(
//      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 deleteReview에 전달
      @PathVariable Long reviewId
  ) {
    Long userId = 1L; //임시 유저 id
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(null));
  }

  @PatchMapping("/{reviewId}")
  @Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다.")
  public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
//      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 updateReview에 전달
      @PathVariable Long reviewId,
      @RequestBody ReviewUpdateRequest request
  ) {
    Long userId = 1L; //임시 유저 id
    ReviewResponse response = reviewService.updateReview(request, reviewId, userId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(response));
  }

  @GetMapping("/{reviewId}")
  @Operation(summary = "리뷰 조회", description = "리뷰 id로 리뷰를 조회합니다.")
  public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
      @PathVariable Long reviewId
  ) {
    ReviewResponse response = reviewService.getReview(reviewId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(response));
  }

//  @GetMapping
//  @Operation(summary = "리뷰 리스트 조회", description = "유저 id로 유저 currentActiveProfile 조회 후 해당 프로필로 리뷰를 조회합니다.")
//  public ResponseEntity<ApiResponse<CursorPageResponse<ReviewResponse>>> getReviews(
//      //      @AuthenticationPrincipal SecurityUser user,
//      //todo: 인증객체 받아와서 getReviews에 전달
//      @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
//      @RequestParam(required = false) String cursor,
//      @RequestParam(defaultValue = "10") Integer size
//  ) {
//    Long userId = 1L; //임시 유저 id
//
//    CursorPageResponse<ReviewResponse> response = reviewService.getReviews(userId, sort, cursor,
//        size);
//
//    return ResponseEntity.status(HttpStatus.OK)
//        .body(ApiResponse.success(response));
//  }

  @PostMapping("/{reviewId}/like")
  @Operation(summary = "리뷰 좋아요 등록", description = "로그인한 사용자가 특정 리뷰에 좋아요를 누릅니다. 이미 좋아요한 리뷰는 중복 등록할 수 없습니다.")
  public ResponseEntity<ApiResponse<ReviewLikeResponse>> likeReview(
      //      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 전달
      @PathVariable Long reviewId
  ) {
    Long userId = 1L; //임시 유저 id

    ReviewLikeResponse response = reviewService.likeReview(reviewId, userId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
  }

  @DeleteMapping("/{reviewId}/like")
  @Operation(summary = "리뷰 좋아요 취소", description = "로그인한 사용자가 특정 리뷰에 좋아요를 누른 상태에서 다시 좋아요를 누르면 취소됩니다.")
  public ResponseEntity<ApiResponse<ReviewLikeResponse>> unLikeReview(
      //      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 전달
      @PathVariable Long reviewId
  ) {
    Long userId = 1L; //임시 유저 id

    ReviewLikeResponse response = reviewService.unlikeReview(reviewId, userId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(response));
  }

  @GetMapping("/{reviewId}/likes")
  @Operation(summary = "리뷰 좋아요 수 조회", description = "특정 리뷰에 달린 좋아요의 수를 조회합니다.")
  public ResponseEntity<ApiResponse<ReviewLikeResponse>> getReviewLikeCount(
      //      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 전달
      @PathVariable Long reviewId
  ) {
    Long userId = 1L; //임시 유저 id

    ReviewLikeResponse response = reviewService.getReviewLikeCount(reviewId, userId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(response));
  }
}
