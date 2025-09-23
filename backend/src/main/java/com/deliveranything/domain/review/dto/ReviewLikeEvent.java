package com.deliveranything.domain.review.dto;

public record ReviewLikeEvent(
    Long reviewId,
    Long likeCount
) {}
