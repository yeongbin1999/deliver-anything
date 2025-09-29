package com.deliveranything.domain.product.product.controller;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.product.dto.ProductDetailResponse;
import com.deliveranything.domain.product.product.dto.ProductResponse;
import com.deliveranything.domain.product.product.dto.ProductSearchRequest;
import com.deliveranything.domain.product.product.dto.ProductUpdateRequest;
import com.deliveranything.domain.product.product.service.ProductService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    ProductResponse productResponse = productService.createProduct(storeId, request);

    return ResponseEntity.created(URI.create("/api/v1/products/" + productResponse.id()))
        .body(ApiResponse.success(productResponse));
  }

  @GetMapping("/stores/{storeId}/products")
  public ResponseEntity<ApiResponse<CursorPageResponse<ProductResponse>>> searchProducts(
      @PathVariable Long storeId,
      @Valid @ModelAttribute ProductSearchRequest request
  ) {
    Slice<ProductResponse> results = productService.searchProducts(storeId, request);

    String nextToken = null;
    if (results.hasNext()) {
      ProductResponse lastProduct = results.getContent().getLast();
      nextToken = String.valueOf(lastProduct.id());
    }

    CursorPageResponse<ProductResponse> response = new CursorPageResponse<>(
        results.getContent(),
        nextToken,
        results.hasNext()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
      @PathVariable Long productId
  ) {
    ProductDetailResponse productResponse = productService.findById(productId);
    return ResponseEntity.ok(ApiResponse.success(productResponse));
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable Long productId,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(productId, request)));
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<Void>> deleteStore(
      @PathVariable Long productId
  ) {
    productService.deleteProduct(productId);
    return ResponseEntity.status(204).body(ApiResponse.success());
  }
}
