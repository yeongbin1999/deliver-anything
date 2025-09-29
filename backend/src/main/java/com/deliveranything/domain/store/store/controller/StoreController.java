package com.deliveranything.domain.store.store.controller;

import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.dto.StoreResponse;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.common.ApiResponse;
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
@RequestMapping("api/v1/stores")
public class StoreController {

  private final StoreService storeService;

  @PostMapping
  public ResponseEntity<ApiResponse<Long>> createStore(
      @Valid @RequestBody StoreCreateRequest request) {
    Long storeId = storeService.createStore(request);
    return ResponseEntity.created(URI.create("/api/v1/stores/" + storeId))
        .body(ApiResponse.success(storeId));
  }

  @GetMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable Long storeId) {
    return ResponseEntity.ok(ApiResponse.success(storeService.findById(storeId)));
  }

  @PutMapping("/{storeId}")
  public ResponseEntity<ApiResponse<Long>> updateStore(
      @PathVariable Long storeId, @Valid @RequestBody StoreUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, request)));
  }

  @DeleteMapping("/{storeId}")
  public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
    storeService.deleteStore(storeId);
    return ResponseEntity.status(204).body(ApiResponse.success());
  }
}
