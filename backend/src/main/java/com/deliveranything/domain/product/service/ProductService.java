package com.deliveranything.domain.product.service;

import com.deliveranything.domain.product.entity.Product;

public interface ProductService {

  Product getProduct(Long productId);
}