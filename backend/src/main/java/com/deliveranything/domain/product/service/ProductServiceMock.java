package com.deliveranything.domain.product.service;

import com.deliveranything.domain.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceMock implements ProductService {

  @Override
  public Product getProduct(Long productId) {
    log.warn("ProductServiceMock is being used - replace with actual implementation");
    return Product.createMock(productId);
  }
}