package com.deliveranything.domain.review.dto;

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
}
