package com.deliveranything.domain.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import com.deliveranything.domain.review.factory.ReviewFactory;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ApiV1ReviewControllerTest {

  @Autowired
  private ReviewService reviewService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Test
  @DisplayName("리뷰 등록 - 정상")
  public void createReview() {
    Long userId = 1L; //임시

    User user = User.builder()
        .email("test@example.com")
        .name("testUser")
        .password("testPassword")
        .phoneNumber("testPhoneNumber")
        .socialProvider(null)
        .build();

    userRepository.save(user);

    ReviewCreateRequest request = ReviewFactory.createReviews(1).getFirst();

    reviewService.createReview(request, userId);

    // 리뷰 등록 호출
    ReviewCreateResponse response = reviewService.createReview(request, userId);

    // 검증
    assertNotNull(response.id());
    assertEquals(1, response.rating());
    assertEquals("test comment1", response.comment());
  }

  @Test
  @DisplayName("리뷰 삭제 - 정상")
  public void deleteReview() {
    Long userId = 1L; // 임시

    User user = User.builder()
        .email("test@example.com")
        .name("testUser")
        .password("testPassword")
        .phoneNumber("testPhoneNumber")
        .socialProvider(null)
        .build();

    userRepository.save(user);

    ReviewCreateRequest request = ReviewFactory.createReviews(1).get(0);

    ReviewCreateResponse response = reviewService.createReview(request, userId);

    // 삭제
    reviewService.deleteReview(userId, response.id());

    // 삭제 여부 확인
    boolean exists = reviewRepository.findById(response.id()).isPresent();
    assertFalse(exists, "리뷰가 삭제되어야 합니다");
  }

}
