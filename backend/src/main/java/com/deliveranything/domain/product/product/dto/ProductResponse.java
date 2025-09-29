package com.deliveranything.domain.product.product.dto;

import com.deliveranything.domain.product.product.entity.Product;

public record ProductResponse(
    Long id,
    String name,
    String description,
    Integer price,
    String imageUrl,
    Integer stockQuantity
) {

  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getImageUrl(),
        product.getStock().getQuantity()
    );
  }
}