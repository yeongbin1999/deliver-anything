package com.deliveranything.domain.reviews.controller;

import com.deliveranything.domain.reviews.dto.ReviewCreateRequest;
import com.deliveranything.domain.reviews.dto.ReviewCreateResponse;
import com.deliveranything.domain.reviews.entity.Review;
import com.deliveranything.domain.reviews.service.ReviewService;
import com.deliveranything.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reviews")
public class ApiV1ReviewController {
  private final ReviewService reviewService;

  //리뷰 생성
  @PostMapping
  public ApiResponse<ReviewCreateResponse> createReview(@RequestBody ReviewCreateRequest request
//      , @AuthenticationPrincipal SecurityUser user
      //todo: 인증객체 받아와서 createReview에 전달
  ) {
    Long userId = 1L; //임시 유저 id
    ReviewCreateResponse response = reviewService.createReview(request, userId);
    return ApiResponse.success(response);
  }
}
