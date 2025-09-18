package com.deliveranything.domain.review.controller;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reviews")
public class ApiV1ReviewController {

  private final ReviewService reviewService;

  //리뷰 생성 - 201 Created
  @PostMapping
  public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
      @RequestBody ReviewCreateRequest request
//      , @AuthenticationPrincipal SecurityUser user
      //todo: 인증객체 받아와서 createReview에 전달
  ) {
    Long userId = 1L; //임시 유저 id
    ReviewCreateResponse response = reviewService.createReview(request, userId);
    return ResponseEntity
        .status(201)
        .body(ApiResponse.success(response));
  }

  //리뷰 삭제 - 204 No content
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<ApiResponse<String>> deleteReview(
//      @AuthenticationPrincipal SecurityUser user,
      //todo: 인증객체 받아와서 deleteReview에 전달
      @PathVariable Long reviewId
  ) {
    Long userId = 1L; //임시 유저 id
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(null));
  }
}
