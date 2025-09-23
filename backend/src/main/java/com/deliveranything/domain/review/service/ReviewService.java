package com.deliveranything.domain.review.service;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.dto.ReviewCreateResponse;
import com.deliveranything.domain.review.dto.ReviewResponse;
import com.deliveranything.domain.review.dto.ReviewUpdateRequest;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.entity.ReviewPhoto;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.review.repository.ReviewPhotoRepository;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.CustomerProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.domain.user.service.UserService;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
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
  private final StoreRepository storeRepository;

  //============================메인 API 메서드==================================
  /* 리뷰 생성 */
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    //유저 존재 여부 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

  /* 내 리뷰 리스트 조회 */
  public CursorPageResponse<ReviewResponse> getReviews(Long userId, ReviewSortType sort,
      String cursor, Integer size) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    ProfileType profileType = user.getCurrentActiveProfile();
    String[] decodedCursor = CursorUtil.decode(cursor);

    List<ReviewResponse> reviewList = getReviewsByProfile(profileType, user, sort, decodedCursor,
        size);

    return toCursorPage(reviewList, sort, size);
  }


  /* 상점 리뷰 리스트 조회 */
  public CursorPageResponse<ReviewResponse> getStoreReviews(Long storeId, ReviewSortType sort,
      String cursor, Integer size) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

    String[] decodedCursor = CursorUtil.decode(cursor);

    List<ReviewResponse> reviewList = getReviewsByStore(store, sort, decodedCursor, size);

    return toCursorPage(reviewList, sort, size);
  }


  //=============================편의 메서드====================================
  /* 리뷰 사진 URL 리스트 반환 */
  @Transactional(readOnly = true)
  public List<String> getReviewPhotoUrlList(Review review) {
    return review.getReviewPhotos().stream()
        .map(ReviewPhoto::getPhotoUrl)
        .toList();
  }

  //리뷰 권한 확인 (작성자 확인)
  @Transactional(readOnly = true)
  public boolean verifyReviewAuth(Review review, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    CustomerProfile customerProfile = user.getCustomerProfile();

    return review.getCustomerProfile().getId().equals(customerProfile.getId());
  }

  private List<ReviewResponse> getReviewsByProfile(ProfileType profileType, User user,
      ReviewSortType sort, String[] cursor, int size) {
    List<Review> reviews = reviewRepository.findReviewsByProfile(
        user,
        profileType, // ProfileType 전달
        sort,        // ReviewSortType 전달
        cursor,
        size
    );

    return reviews.stream()
        .map(it -> ReviewResponse.from(it, getReviewPhotoUrlList(it)))
        .toList();
  }

  private List<ReviewResponse> getReviewsByStore(Store store, ReviewSortType sort, String[] cursor,
      int size) {
    List<Review> reviews = reviewRepository.findReviewsByStore(
        store,
        sort,
        cursor,
        size
    );

    return reviews.stream()
        .map(it -> ReviewResponse.from(it, getReviewPhotoUrlList(it)))
        .toList();
  }

  //Cursor 기반 페이지네이션 공용 처리
  private CursorPageResponse<ReviewResponse> toCursorPage(
      List<ReviewResponse> reviewList,
      ReviewSortType sort,
      int size
  ) {
    boolean hasNext = reviewList.size() > size;
    List<ReviewResponse> result = hasNext ? reviewList.subList(0, size) : reviewList;

    String nextPageToken = null;
    if (!reviewList.isEmpty()) {
      ReviewResponse lastReview = reviewList.get(reviewList.size() - 1);

      String cursorValue = switch (sort) {
        case LATEST, OLDEST -> lastReview.createdAt().toString();
        case RATING_DESC, RATING_ASC -> String.valueOf(lastReview.rating());
      };

      nextPageToken = CursorUtil.encode(cursorValue, lastReview.id());
    }

    return new CursorPageResponse<>(result, nextPageToken, hasNext);
  }
}
