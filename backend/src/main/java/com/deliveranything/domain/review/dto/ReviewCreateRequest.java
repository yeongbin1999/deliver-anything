package com.deliveranything.domain.review.dto;

import com.deliveranything.domain.review.enums.ReviewTargetType;

public record ReviewCreateRequest(
    int rating,
    String comment,
    String[] photoUrls,
    ReviewTargetType targetType,
    Long targetId
) {

}
