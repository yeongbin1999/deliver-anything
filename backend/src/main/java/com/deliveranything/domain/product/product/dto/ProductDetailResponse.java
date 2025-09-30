package com.deliveranything.domain.product.product.dto;

import com.deliveranything.domain.product.product.entity.Product;

public record ProductDetailResponse(
    Long id,
    String name,
    String description,
    Integer price,
    String imageUrl,
    Integer stockQuantity
) {

  public static ProductDetailResponse from(Product product) {
    return new ProductDetailResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getImageUrl(),
        product.getStock().getQuantity()
    );
  }
}