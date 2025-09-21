package com.deliveranything.domain.store.store.dto;

public record StoreCategoryResponse(
    String name,
    String displayName,
    int sortOrder
) {

}