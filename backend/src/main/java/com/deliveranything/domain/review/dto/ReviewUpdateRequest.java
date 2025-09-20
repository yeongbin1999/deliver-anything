package com.deliveranything.domain.review.dto;

public record ReviewUpdateRequest(
    Integer rating,
    String comment,
    String[] photoUrls
) {

}
