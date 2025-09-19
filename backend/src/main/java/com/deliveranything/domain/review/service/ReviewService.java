package com.deliveranything.domain.review.service;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.entity.ReviewPhoto;
import com.deliveranything.domain.review.repository.ReviewPhotoRepository;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.repository.UserRepository;
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
  private final CustomerProfileRepository customerProfileRepository;

  //============================메인 API 메서드==================================
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    CustomerProfile customerProfile = customerProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_NOT_FOUND));


    //리뷰 생성
    Review review = Review.from(request, customerProfile);
    reviewRepository.save(review);

    //리뷰 사진 객체 생성
    List<ReviewPhoto> reviewPhotos = Arrays.stream(request.photoUrls())
        .map(url -> ReviewPhoto.builder()
            .photoUrl(url)
            .review(review)
            .build())
        .toList();

    reviewPhotoRepository.saveAll(reviewPhotos);

    List<String> reviewPhotoUrls = getReviewPhotoUrlList(review);

    return ReviewCreateResponse.from(review, reviewPhotoUrls);
  }

  public void deleteReview(Long userId, Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    if (verifyReviewAuth(review, userId)) {
      reviewRepository.delete(review);
    } else {
      throw new CustomException(ErrorCode.REVIEW_NO_PERMISSION);
    }
  }

  public ReviewResponse getReview(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    List<String> reviewPhotoUrls = getReviewPhotoUrlList(review);

    return ReviewResponse.from(review, reviewPhotoUrls);
  }

  //=============================편의 메서드====================================
  @Transactional(readOnly = true)
  public List<String> getReviewPhotoUrlList(Review review) {
    return reviewPhotoRepository.findAllByReview(review).stream()
        .map(ReviewPhoto::getPhotoUrl)
        .toList();
  }

  //리뷰 수정, 삭제 등 권한 체크용 메서드
  @Transactional(readOnly = true)
  public boolean verifyReviewAuth(Review review, Long userId) {
    CustomerProfile customerProfile = customerProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_NOT_FOUND));

    return review.getCustomerProfile().getId().equals(user.getId());
  }
}
