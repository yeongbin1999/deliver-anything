package com.deliveranything.domain.search.store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StoreSearchRequest(
    @NotNull(message = "위도(lat)는 필수 입력 값입니다.")
    Double lat,

    @NotNull(message = "경도(lng)는 필수 입력 값입니다.")
    Double lng,

    Long categoryId,

    @Size(max = 50, message = "검색어는 50자를 초과할 수 없습니다.")
    String searchText,

    @Min(value = 0, message = "거리는 0km 이상이어야 합니다.")
    @Max(value = 10, message = "거리는 10km를 초과할 수 없습니다.")
    Double distanceKm,

    @Min(value = 1, message = "limit 값은 1 이상이어야 합니다.")
    @Max(value = 50, message = "limit 값은 50을 초과할 수 없습니다.")
    Integer limit,

    String nextPageToken
) {
  public StoreSearchRequest {
    if (distanceKm == null) { distanceKm = 7.0; }
    if (limit == null) { limit = 20; }
  }
}