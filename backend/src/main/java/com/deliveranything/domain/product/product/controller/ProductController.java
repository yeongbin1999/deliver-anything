package com.deliveranything.domain.product.product.controller;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.product.dto.ProductDetailResponse;
import com.deliveranything.domain.product.product.dto.ProductResponse;
import com.deliveranything.domain.product.product.dto.ProductSearchRequest;
import com.deliveranything.domain.product.product.dto.ProductUpdateRequest;
import com.deliveranything.domain.product.product.service.ProductService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "상품 관련 API", description = "상품 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "상품 생성", description = "새로운 상품을 생성합니다.")
  @PostMapping("/stores/{storeId}/products")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Parameter(description = "상품을 등록할 상점 ID") @PathVariable Long storeId,
      @Valid @RequestBody ProductCreateRequest request
  ) {
    ProductResponse productResponse = productService.createProduct(storeId, request);

    return ResponseEntity.created(URI.create("/api/v1/products/" + productResponse.id()))
        .body(ApiResponse.success(productResponse));
  }

  @Operation(summary = "상품 목록 조회", description = "특정 상점의 상품 목록을 조회합니다.")
  @GetMapping("/stores/{storeId}/products")
  public ResponseEntity<ApiResponse<CursorPageResponse<ProductResponse>>> searchProducts(
      @Parameter(description = "상품을 조회할 상점 ID") @PathVariable Long storeId,
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

  @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
  @GetMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
      @Parameter(description = "조회할 상품 ID") @PathVariable Long productId
  ) {
    ProductDetailResponse productResponse = productService.getProduct(productId);
    return ResponseEntity.ok(ApiResponse.success(productResponse));
  }

  @Operation(summary = "상품 정보 수정", description = "특정 상품의 정보를 수정합니다.")
  @PutMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @Parameter(description = "수정할 상품 ID") @PathVariable Long productId,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(productId, request)));
  }

  @Operation(summary = "상품 삭제", description = "특정 상품을 삭제합니다.")
  @DeleteMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @Parameter(description = "삭제할 상품 ID") @PathVariable Long productId
  ) {
    productService.deleteProduct(productId);
    return ResponseEntity.status(204).body(ApiResponse.success());
  }
}
