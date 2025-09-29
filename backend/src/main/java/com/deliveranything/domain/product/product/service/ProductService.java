package com.deliveranything.domain.product.product.service;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.product.dto.ProductDetailResponse;
import com.deliveranything.domain.product.product.dto.ProductResponse;
import com.deliveranything.domain.product.product.dto.ProductSearchRequest;
import com.deliveranything.domain.product.product.dto.ProductUpdateRequest;
import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.domain.product.product.repository.ProductRepository;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final StoreService storeService;
  private final KeywordGenerationService keywordGenerationService;

  @Transactional
  public ProductResponse createProduct(Long storeId, ProductCreateRequest request) {
    Store store = storeService.findById(storeId);

    Product product = Product.builder()
        .store(store)
        .name(request.name())
        .description(request.description())
        .price(request.price())
        .imageUrl(request.imageUrl())
        .initialStock(request.initialStock())
        .build();

    Product saveProduct = productRepository.save(product);
    keywordGenerationService.generateAndSaveKeywords(saveProduct.getId());

    return ProductResponse.from(saveProduct);
  }

  @Transactional
  public void deleteProduct(Long productId) {
    Product product = findById(productId);
    productRepository.delete(product);
  }

  @Transactional
  public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
    Product product = findById(productId);

    String oldName = product.getName();
    String oldDescription = product.getDescription();

    product.update(request.name(), request.description(), request.price(), request.imageUrl());

    if (!oldName.equals(request.name()) || !oldDescription.equals(request.description())) {
      keywordGenerationService.generateAndSaveKeywords(product.getId());
    }

    return ProductResponse.from(product);
  }

  @Transactional(readOnly = true)
  public ProductDetailResponse findById(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    return ProductDetailResponse.from(product);
  }

  @Transactional(readOnly = true)
  public Slice<ProductResponse> searchProducts(Long storeId, ProductSearchRequest request) {
    storeService.findById(storeId);

    Slice<Product> results = productRepository.search(storeId, request);

    return results.map(ProductResponse::from);
  }
}