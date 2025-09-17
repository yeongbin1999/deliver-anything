package com.deliveranything.domain.reviews.service;

import com.deliveranything.domain.reviews.dto.ReviewCreateRequest;
import com.deliveranything.domain.reviews.dto.ReviewCreateResponse;
import com.deliveranything.domain.reviews.entity.Review;
import com.deliveranything.domain.reviews.entity.ReviewPhoto;
import com.deliveranything.domain.reviews.repository.ReviewPhotoRepository;
import com.deliveranything.domain.reviews.repository.ReviewRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final ReviewPhotoRepository reviewPhotoRepository;


  public ReviewCreateResponse createReview(ReviewCreateRequest request) {
    //리뷰 생성
    Review review = Review.builder()
        .targetType(request.targetType())
        .user() //파라미터로 유저 받아오기
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
    }
  }
}
