package com.deliveranything.domain.store.store.controller;

import com.deliveranything.domain.store.store.dto.StoreResponse;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stores")
public class StoreController {

  private final StoreService storeService;

//  현재 필요없는 API
//  @PostMapping
//  public ResponseEntity<ApiResponse<Long>> createStore(
//      @Valid @RequestBody StoreCreateRequest request
//  ) {
//    Long storeId = storeService.createStore(request);
//    return ResponseEntity.created(URI.create("/api/v1/stores/" + storeId))
//        .body(ApiResponse.success(storeId));
//  }

  @GetMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreResponse>> getStore(
      @PathVariable Long storeId
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.getStore(storeId)));
  }

  @PutMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
      @PathVariable Long storeId,
      @Valid @RequestBody StoreUpdateRequest request
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, request)));
  }

//  현재 필요없는 API
//  @DeleteMapping("/{storeId}")
//  public ResponseEntity<ApiResponse<Void>> deleteStore(
//      @PathVariable Long storeId
//  ) {
//    storeService.deleteStore(storeId);
//    return ResponseEntity.status(204).body(ApiResponse.success());
//  }

  @PostMapping("/{storeId}/toggle-status")
  public ResponseEntity<ApiResponse<StoreResponse>> toggleStoreStatus(
      @PathVariable Long storeId
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.toggleStoreStatus(storeId)));
  }
}
