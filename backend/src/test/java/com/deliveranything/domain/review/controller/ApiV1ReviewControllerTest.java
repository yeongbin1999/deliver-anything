package com.deliveranything.domain.review.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.dto.ReviewUpdateRequest;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import com.deliveranything.domain.review.factory.ReviewFactory;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.review.service.ReviewService;
import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import java.util.List;
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

  /**
   * 리뷰 수정 성공 테스트
   */
  @Test
  @DisplayName("리뷰 수정 - 정상")
  public void updateReview_success() {
    // given : 리뷰 작성 유저 생성
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

    // 리뷰 생성
    ReviewCreateRequest createRequest = ReviewFactory.createReviews(1).getFirst();
    ReviewCreateResponse createdReview = reviewService.createReview(createRequest, user.getId());

    // 수정 요청 DTO
    ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
        5, // rating 수정
        "수정된 댓글", // comment 수정
        new String[]{"new_photo1.jpg", "new_photo2.jpg"} // 사진 수정
    );

    // when : 리뷰 수정
    ReviewResponse updatedReview = reviewService.updateReview(
        updateRequest,
        user.getId(),
        createdReview.id()
    );

    // then : 수정 결과 검증
    assertNotNull(updatedReview.id());
    assertEquals(5, updatedReview.rating());
    assertEquals("수정된 댓글", updatedReview.comment());
    assertEquals(2, updatedReview.photoUrls().size());
    assertTrue(updatedReview.photoUrls().contains("new_photo1.jpg"));
    assertTrue(updatedReview.photoUrls().contains("new_photo2.jpg"));
  }

  /**
   * 다른 유저가 작성한 리뷰 수정 시도 → 권한 없음 예외 발생
   */
  @Test
  @DisplayName("리뷰 삭제 - 권한 없는 유저의 요청")
  public void updateReview_userWithoutPermission_throwsException() {
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

    ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
        5, // rating 수정
        "수정된 댓글", // comment 수정
        new String[]{"new_photo1.jpg", "new_photo2.jpg"} // 사진 수정
    );

    // when & then : 권한 없는 유저가 삭제 시도
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.updateReview(updateRequest, otherUser.getId(), createdReview.id());
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    assertEquals("REVIEW-403", exception.getCode());
    assertEquals("리뷰를 관리할 권한이 없습니다.", exception.getMessage());
  }

  /**
   * 리뷰 목록 조회 성공 테스트
   */
  @Test
  @DisplayName("리뷰 목록 조회 - 정상")
  public void getReviewsByUser_success() {
    // given : 리뷰 작성 유저 생성
    User user = User.builder()
        .email("reviewer@example.com")
        .name("reviewerUser")
        .password("reviewerPassword")
        .phoneNumber("010-2222-3333")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(user)
        .nickname("reviewerProfile")
        .build();
    user.setCustomerProfile(profile);
    user.switchProfile(ProfileType.CUSTOMER);
    userRepository.save(user);

    // 리뷰 여러 개 생성
    List<ReviewCreateRequest> requests = ReviewFactory.createReviews(3); // 3개의 리뷰 생성
    for (ReviewCreateRequest rq : requests) {
      reviewService.createReview(rq, user.getId());
    }

    // when : 리뷰 목록 조회
    CursorPageResponse<ReviewResponse> responses = reviewService.getReviews(user.getId(), ReviewSortType.RATING_ASC, null, 10);
    // getReviewsByUser: 유저 기준, 최대 10개, 커서 없음

    // then : 조회 결과 검증
    assertEquals(3, responses.content().size(), "리뷰 개수가 일치해야 합니다");

    for (int i = 0; i < responses.content().size(); i++) {
      ReviewCreateRequest rq = requests.get(i);
      ReviewResponse rs = responses.content().get(i);

      assertNotNull(rs.id(), "리뷰 ID가 존재해야 합니다");
      assertEquals(rq.rating(), rs.rating(), "리뷰 평점이 일치해야 합니다");
      assertEquals(rq.comment(), rs.comment(), "리뷰 코멘트가 일치해야 합니다");
    }
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 정렬 순서 검증")
  public void getReviewsByUser_ordering_success() {
    // given : 리뷰 작성 유저 생성
    User user = User.builder()
        .email("reviewer@example.com")
        .name("reviewerUser")
        .password("reviewerPassword")
        .phoneNumber("010-2222-3333")
        .socialProvider(null)
        .build();
    CustomerProfile profile = CustomerProfile.builder()
        .user(user)
        .nickname("reviewerProfile")
        .build();
    user.setCustomerProfile(profile);
    user.switchProfile(ProfileType.CUSTOMER);
    userRepository.save(user);

    // 리뷰 여러 개 생성 (랜덤 평점)
    List<ReviewCreateRequest> requests = List.of(
        new ReviewCreateRequest(5, "comment5", new String[]{}, ReviewTargetType.STORE, 1L),
        new ReviewCreateRequest(3, "comment3", new String[]{}, ReviewTargetType.STORE, 1L),
        new ReviewCreateRequest(4, "comment4", new String[]{}, ReviewTargetType.STORE, 1L)
    );

    for (ReviewCreateRequest rq : requests) {
      reviewService.createReview(rq, user.getId());
    }

    // when : 리뷰 목록 조회 (평점 오름차순)
    CursorPageResponse<ReviewResponse> responses = reviewService.getReviews(user.getId(), ReviewSortType.RATING_ASC, null, 10);

    // then : 개수 검증
    assertEquals(3, responses.content().size(), "리뷰 개수가 일치해야 합니다");

    // 순서 검증 (평점 오름차순)
    List<Integer> ratings = responses.content().stream()
        .map(ReviewResponse::rating)
        .toList();

    List<Integer> sortedRatings = ratings.stream().sorted().toList();

    assertEquals(sortedRatings, ratings, "리뷰가 평점 오름차순으로 정렬되어야 합니다");

    // 추가 필드 검증
    for (int i = 0; i < responses.content().size(); i++) {
      ReviewResponse rs = responses.content().get(i);
      assertNotNull(rs.id(), "리뷰 ID가 존재해야 합니다");
      assertNotNull(rs.comment(), "리뷰 코멘트가 존재해야 합니다");
    }
  }

}
