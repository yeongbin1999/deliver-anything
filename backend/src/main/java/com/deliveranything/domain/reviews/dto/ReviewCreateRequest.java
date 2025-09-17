package com.deliveranything.domain.reviews.dto;

import com.deliveranything.domain.reviews.enums.ReviewTargetType;

public record ReviewCreateRequest(
   int rating,
   String comment,
   String[] photoUrls,
   ReviewTargetType targetType,
   Long targetId
) {}
