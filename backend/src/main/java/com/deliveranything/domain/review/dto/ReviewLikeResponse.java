package com.deliveranything.domain.review.dto;

public record ReviewLikeResponse(
    Long reviewId,
    Long likeCount,
    Boolean likedByMe
) {}
