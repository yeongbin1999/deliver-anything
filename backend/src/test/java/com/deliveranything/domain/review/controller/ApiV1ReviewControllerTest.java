package com.deliveranything.domain.review.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.factory.ReviewFactory;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
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

  /**
   * 리뷰 등록 성공 테스트
   */
  @Test
  @DisplayName("리뷰 등록 - 정상")
  public void createReview() {
    // given : 유저 생성 및 저장
    User user = User.builder()
        .email("test@example.com")
        .name("testUser")
        .password("testPassword")
        .phoneNumber("testPhoneNumber")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(user)
        .nickname("testUser")
        .build();
    user.setCustomerProfile(profile);
    userRepository.save(user);

    // when : 리뷰 등록 요청
    ReviewCreateRequest request = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse response = reviewService.createReview(request, user.getId());

    // then : 등록된 리뷰 검증
    assertNotNull(response.id());
    assertEquals(1, response.rating());
    assertEquals("test comment1", response.comment());
  }

  /**
   * 리뷰 삭제 성공 테스트
   */
  @Test
  @DisplayName("리뷰 삭제 - 정상")
  public void deleteReview() {
    // given : 유저 및 리뷰 생성
    User user = User.builder()
        .email("test@example.com")
        .name("testUser")
        .password("testPassword")
        .phoneNumber("testPhoneNumber")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(user)
        .nickname("testUser")
        .build();
    user.setCustomerProfile(profile);
    userRepository.save(user);

    ReviewCreateRequest request = ReviewFactory.createReviews(1).get(0);
    ReviewCreateResponse response = reviewService.createReview(request, user.getId());

    // when : 리뷰 삭제
    reviewService.deleteReview(user.getId(), response.id());

    // then : 삭제 여부 검증
    boolean exists = reviewRepository.findById(response.id()).isPresent();
    assertFalse(exists, "리뷰가 삭제되어야 합니다");
  }

  /**
   * 존재하지 않는 리뷰 삭제 시도 → 예외 발생
   */
  @Test
  @DisplayName("리뷰 삭제 - 존재하지 않는 리뷰")
  public void deleteReview_nonExistentReview_throwsException() {
    // when & then
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(1L, 1L);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("REVIEW-404", exception.getCode());
    assertEquals("리뷰를 찾을 수 없습니다.", exception.getMessage());
  }

  /**
   * 다른 유저가 작성한 리뷰 삭제 시도 → 권한 없음 예외 발생
   */
  @Test
  @DisplayName("리뷰 삭제 - 권한 없는 유저의 요청")
  public void deleteReview_userWithoutPermission_throwsException() {
    // given : 리뷰 작성 유저 생성
    User owner = User.builder()
        .email("owner@example.com")
        .name("ownerUser")
        .password("ownerPassword")
        .phoneNumber("010-0000-0000")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(owner)
        .nickname("testUser")
        .build();
    owner.setCustomerProfile(profile);
    userRepository.save(owner);

    // 리뷰 생성
    ReviewCreateRequest request = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse createdReview = reviewService.createReview(request, owner.getId());

    // 다른 유저 생성
    User otherUser = User.builder()
        .email("other@example.com")
        .name("otherUser")
        .password("otherPassword")
        .phoneNumber("010-1111-1111")
        .socialProvider(null)
        .build();
    CustomerProfile otherProfile = CustomerProfile.builder()
        .user(otherUser)
        .nickname("testUser2")
        .build();
    otherUser.setCustomerProfile(otherProfile);
    userRepository.save(otherUser);

    // when & then : 권한 없는 유저가 삭제 시도
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(otherUser.getId(), createdReview.id());
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    assertEquals("REVIEW-403", exception.getCode());
    assertEquals("리뷰를 관리할 권한이 없습니다.", exception.getMessage());
  }

  /**
   * 존재하지 않는 리뷰 조회 시도 → 예외 발생
   */
  @Test
  @DisplayName("리뷰 조회 - 존재하지 않는 리뷰")
  public void getReview_nonExistentReview_throwsException() {
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.getReview(1L);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("REVIEW-404", exception.getCode());
    assertEquals("리뷰를 찾을 수 없습니다.", exception.getMessage());
  }

  /**
   * 리뷰 단건 조회 성공 테스트
   */
  @Test
  @DisplayName("리뷰 단건 조회 - 정상")
  public void getReview() {
    // given : 유저 및 리뷰 생성
    User user = User.builder()
        .email("owner@example.com")
        .name("ownerUser")
        .password("ownerPassword")
        .phoneNumber("010-0000-0000")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(user)
        .nickname("testUser")
        .build();
    user.setCustomerProfile(profile);
    userRepository.save(user);

    ReviewCreateRequest reviewRq = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse reviewRs = reviewService.createReview(reviewRq, user.getId());

    // when : 리뷰 단건 조회
    ReviewResponse response = reviewService.getReview(reviewRs.id());

    // then : 조회 결과 검증
    assertNotNull(response.id());
    assertEquals(reviewRs.id(), response.id());
    assertEquals(reviewRq.rating(), response.rating());
    assertEquals(reviewRq.comment(), response.comment());

    // TODO: jwtConfig/SecurityConfig 적용 후 생성일/작성자 검증 추가
  }
}
