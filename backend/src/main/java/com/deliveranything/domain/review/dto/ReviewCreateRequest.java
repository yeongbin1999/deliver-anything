package com.deliveranything.domain.review.dto;

import com.deliveranything.domain.review.enums.ReviewTargetType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 생성 요청 DTO")
public record ReviewCreateRequest(
    @Schema(description = "별점", example = "5")
    Integer rating,
    @Schema(description = "코멘트", example = "배송이 빨랐습니다.")
    String comment,
    @Schema(description = "리뷰 사진 목록", example = "[\"https://example.com/photo1.jpg\", \"https://example.com/photo2.jpg\"]")
    String[] photoUrls,
    @Schema(description = "리뷰 대상 분류", example = "RIDER", allowableValues = {"RIDER","STORE"})
    ReviewTargetType targetType,
    @Schema(description = "리뷰 대상 id (상점 id or 배달원프로필 id)", example = "5")
    Long targetId
) {

}
