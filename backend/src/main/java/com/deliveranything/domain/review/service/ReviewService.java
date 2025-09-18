package com.deliveranything.domain.review.service;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.entity.ReviewPhoto;
import com.deliveranything.domain.review.repository.ReviewPhotoRepository;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.user.entity.User;
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

  //============================메인 API 메서드==================================
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalStateException("TODO: UserErrorCode.USER_NOT_FOUND 사용 예정"));

    //리뷰 생성
    Review review = Review.builder()
        .targetType(request.targetType())
        .user(user)
        .comment(request.comment())
        .rating(request.rating())
        .targetId(request.targetId())
        .build();

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

    return new ReviewCreateResponse(review.getId(), review.getRating(), review.getComment(),
        reviewPhotoUrls, review.getTargetType(), review.getTargetId());
  }

  public void deleteReview(Long userId, Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    if (verifyReviewAuth(userId, reviewId)) {
      reviewRepository.delete(review);
    } else {
      throw new CustomException(ErrorCode.REVIEW_NO_PERMISSION);
    }
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
  public boolean verifyReviewAuth(Long reviewId, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalStateException("TODO: UserErrorCode.USER_NOT_FOUND 사용 예정"));

    Review review =  reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    return review.getUser().getId().equals(user.getId());
  }
}
