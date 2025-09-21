package com.deliveranything.domain.store.store.dto;

public record StoreSearchRequest(
    Double lat,
    Double lng,
    Long categoryId,
    String keyword,
    Integer limit,
    String nextPageToken
) {

}