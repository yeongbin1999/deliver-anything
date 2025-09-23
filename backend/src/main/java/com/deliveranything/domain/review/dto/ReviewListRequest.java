package com.deliveranything.domain.review.dto;

import com.deliveranything.domain.review.enums.ReviewSortType;

public record ReviewListRequest(
    ReviewSortType sort,
    String cursor,
    Integer size
) {}
