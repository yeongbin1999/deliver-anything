package com.deliveranything.domain.product.product.controller;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.product.dto.ProductResponse;
import com.deliveranything.domain.product.product.dto.ProductUpdateRequest;
import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.domain.product.product.service.ProductService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

  private final ProductService productService;

  @PostMapping("/stores/{storeId}/products")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @PathVariable Long storeId,
      @Valid @RequestBody ProductCreateRequest request
  ) {
    Product product = productService.createProduct(storeId, request);

    return ResponseEntity.created(URI.create("/api/v1/products/" + product.getId()))
        .body(ApiResponse.success(ProductResponse.from(product)));
  }

  @GetMapping("/stores/{storeId}/products")
  public ResponseEntity<ApiResponse<CursorPageResponse<ProductResponse>>> createProduct(
      @PathVariable Long storeId
  ) {

  }

//  상품 상세가 별도로 없어 당장은 필요없는 API
//  @GetMapping("/products/{productId}")
//  public ResponseEntity<ApiResponse<ProductResponse>> getStore(
//      @PathVariable Long productId
//  ) {
//    Product product = productService.findById(productId);
//
//    return ResponseEntity.ok(ApiResponse.success(ProductResponse.from(product)));
//  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<ProductResponse>> updateStore(
      @PathVariable Long productId,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        ProductResponse.from(productService.updateProduct(productId, request)))
    );
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long productId) {
    productService.deleteProduct(productId);
    return ResponseEntity.status(204).body(ApiResponse.success());
  }
}
