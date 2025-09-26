package com.deliveranything.domain.review.dto;

import com.deliveranything.global.common.CursorPageResponse;

public record ReviewRatingAndListResponseDto(
    Double avgRating,
    CursorPageResponse<ReviewResponse> reviews
) {

}
