package com.deliveranything.domain.review.service;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.dto.ReviewUpdateRequest;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.entity.ReviewPhoto;
import com.deliveranything.domain.review.repository.ReviewPhotoRepository;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.CustomerProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.domain.user.service.UserService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewPhotoRepository reviewPhotoRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final CustomerProfileRepository customerProfileRepository;

  //============================메인 API 메서드==================================
  /* 리뷰 생성 */
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    //유저 존재 여부 확인
    User user = userRepository.findById(userId)
//        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); 관련 ERROR CODE 생길 시 대체
        .orElseThrow(() -> new CustomException(ErrorCode.DEV_NOT_FOUND));

    CustomerProfile customerProfile = user.getCustomerProfile();

    //리뷰 생성 및 저장
    Review review = Review.from(request, customerProfile);
    reviewRepository.save(review);

    //리뷰 사진 생성 및 저장
    List<ReviewPhoto> reviewPhotos = Arrays.stream(request.photoUrls())
        .map(url -> ReviewPhoto.builder()
            .photoUrl(url)
            .review(review)
            .build())
        .toList();
    reviewPhotoRepository.saveAll(reviewPhotos);

    //사진 URL 리스트 반환
    List<String> reviewPhotoUrls = getReviewPhotoUrlList(review);

    return ReviewCreateResponse.from(review, reviewPhotoUrls);
  }

  /* 리뷰 삭제 */
  public void deleteReview(Long userId, Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    if (verifyReviewAuth(review, userId)) {
      reviewRepository.delete(review);
    } else {
      throw new CustomException(ErrorCode.REVIEW_NO_PERMISSION);
    }
  }

  /* 리뷰 수정 */
  public ReviewResponse updateReview(ReviewUpdateRequest request, Long userId, Long reviewId) {
    //유저 존재 여부 확인
    User user = userRepository.findById(userId)
//        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); 관련 ERROR CODE 생길 시 대체
        .orElseThrow(() -> new CustomException(ErrorCode.DEV_NOT_FOUND));

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    //유저 권한 체크
    if (!verifyReviewAuth(review, userId)) {
      throw new CustomException(ErrorCode.REVIEW_NO_PERMISSION);
    }

    //리뷰 업데이트
    review.update(request);
    review.updateReviewPhoto(request.photoUrls());
    reviewRepository.save(review);

    return ReviewResponse.from(review, getReviewPhotoUrlList(review));
  }

  /* 단일 리뷰 조회 */
  public ReviewResponse getReview(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    List<String> reviewPhotoUrls = getReviewPhotoUrlList(review);

    return ReviewResponse.from(review, reviewPhotoUrls);
  }

  //=============================편의 메서드====================================
  /* 리뷰 사진 URL 리스트 반환 */
  @Transactional(readOnly = true)
  public List<String> getReviewPhotoUrlList(Review review) {
    return reviewPhotoRepository.findAllByReview(review).stream()
        .map(ReviewPhoto::getPhotoUrl)
        .toList();
  }

  //리뷰 권한 확인 (작성자 확인)
  @Transactional(readOnly = true)
  public boolean verifyReviewAuth(Review review, Long userId) {
    User user = userRepository.findById(userId)
//        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); 관련 ERROR CODE 생길 시 대체
        .orElseThrow(() -> new CustomException(ErrorCode.DEV_NOT_FOUND));
    CustomerProfile customerProfile =  user.getCustomerProfile();

    return review.getCustomerProfile().getId().equals(customerProfile.getId());
  }
}
