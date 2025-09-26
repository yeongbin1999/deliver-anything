package com.deliveranything.domain.product.product.service;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.domain.product.product.repository.ProductRepository;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final StoreService storeService;

  public Product createProduct(Long storeId, ProductCreateRequest request) {
    Store store = storeService.findById(storeId);

    Product product = Product.of(request, store);

    return null;
  }


}
