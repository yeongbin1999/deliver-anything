package com.deliveranything.domain.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import com.deliveranything.domain.review.factory.ReviewFactory;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
    User user = User.builder()
        .email("test@example.com")
        .name("testUser")
        .password("testPassword")
        .phoneNumber("testPhoneNumber")
        .socialProvider(null)
        .build();

    userRepository.save(user);

    ReviewCreateRequest request = ReviewFactory.createReviews(1).getFirst();

    reviewService.createReview(request, user.getId());

    // 리뷰 등록 호출
    ReviewCreateResponse response = reviewService.createReview(request, user.getId());

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

  @Test
  @DisplayName("리뷰 삭제 - 존재하지 않는 리뷰")
  public void deleteReview_nonExistentReview_throwsException() {
    Long userId = 1L; // 임시

    // 삭제 시도
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(userId, 1L);
    });

    // 오류 메세지 확인
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("REVIEW-404", exception.getCode());
    assertEquals("리뷰를 찾을 수 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("리뷰 삭제 - 권한 없는 유저의 요청")
  public void deleteReview_userWithoutPermission_throwsException() {
    // 1. 리뷰 작성 유저 생성
    User owner = User.builder()
        .email("owner@example.com")
        .name("ownerUser")
        .password("ownerPassword")
        .phoneNumber("010-0000-0000")
        .socialProvider(null)
        .build();
    userRepository.save(owner);

    // 2. 리뷰 생성
    ReviewCreateRequest request = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse createdReview = reviewService.createReview(request, owner.getId());

    // 3. 삭제 시도 유저 생성 (권한 없음)
    User otherUser = User.builder()
        .email("other@example.com")
        .name("otherUser")
        .password("otherPassword")
        .phoneNumber("010-1111-1111")
        .socialProvider(null)
        .build();
    userRepository.save(otherUser);

    // 4. 권한 없는 유저 삭제 시도
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(otherUser.getId(), createdReview.id());
    });

    // 5. 예외 확인
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    assertEquals("REVIEW-403", exception.getCode());
    assertEquals("리뷰를 관리할 권한이 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("리뷰 단건 조회 - 정상")
  public void getReview() {
    User owner = User.builder()
        .email("owner@example.com")
        .name("ownerUser")
        .password("ownerPassword")
        .phoneNumber("010-0000-0000")
        .socialProvider(null)
        .build();
    userRepository.save(owner);

    ReviewCreateRequest reviewRq = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse reviewRs = reviewService.createReview(reviewRq, owner.getId());

    ReviewResponse response = reviewService.getReview(reviewRs.id());

    assertNotNull(response.id());
    assertEquals(reviewRs.id(), response.id());
    assertEquals(reviewRq.rating(), response.rating());
    assertEquals(reviewRq.comment(), response.comment());

    //Todo: jwtConfig/SecurityConfig 생성 후 생성일 관련 검증 추가
  }

}
