package com.deliveranything.domain.store.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreCreateRequest(
    @NotNull Long storeCategoryId,
    @NotBlank String name,
    String description,
    @NotBlank String roadAddr,
    @NotNull Double lat,
    @NotNull Double lng
) {

}
