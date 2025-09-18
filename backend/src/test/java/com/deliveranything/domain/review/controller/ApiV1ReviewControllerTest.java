package com.deliveranything.domain.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.enums.ReviewTargetType;
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
  private ReviewRepository reviewRepository;

  @Autowired
  private UserRepository userRepository;

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

    ReviewCreateRequest request = new ReviewCreateRequest(
        5,
        "안녕하세요",
        new String[]{"url1", "url2"},
        ReviewTargetType.STORE,
        1L
    );


    reviewService.createReview(request, userId);

    // 리뷰 등록 호출
    ReviewCreateResponse response = reviewService.createReview(request, userId);

    // 간단 검증 (예: 리뷰 id가 생성됐는지)
    assertNotNull(response.id());
    assertEquals(5, response.rating());
    assertEquals("안녕하세요", response.comment());
  }
}
