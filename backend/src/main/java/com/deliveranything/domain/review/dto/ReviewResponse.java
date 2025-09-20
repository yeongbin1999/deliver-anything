package com.deliveranything.domain.review.dto;

import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
    Long id,
    int rating,
    String comment,
    List<String> photoUrls,
    ReviewTargetType targetType,
    Long targetId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
    // , UserResponse
) {

  public static ReviewResponse from(Review review, List<String> photoUrls) {
    return new ReviewResponse(
        review.getId(),
        review.getRating(),
        review.getComment(),
        photoUrls,
        review.getTargetType(),
        review.getTargetId(),
        review.getCreatedAt(),
        review.getUpdatedAt()
        //userResponse
    );
  }
}
