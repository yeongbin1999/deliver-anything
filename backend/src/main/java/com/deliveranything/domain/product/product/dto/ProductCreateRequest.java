package com.deliveranything.domain.product.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    @Size(max = 50, message = "상품명은 50자를 초과할 수 없습니다.")
    String name,

    @Size(max = 300, message = "설명은 300자를 초과할 수 없습니다.")
    String description,

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    Integer price,

    @NotBlank(message = "이미지 URL은 필수 입력 값입니다.")
    String imageUrl,

    @NotNull(message = "재고 수량은 필수 입력 값입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    Long StockQuantity
) {
  public ProductCreateRequest {
    if (description == null) { description = ""; }
  }
}