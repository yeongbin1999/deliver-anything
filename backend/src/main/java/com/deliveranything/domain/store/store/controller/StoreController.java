package com.deliveranything.domain.store.store.controller;

import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.dto.StoreResponse;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.security.SecurityUser;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  @PreAuthorize("@profileSecurity.isSeller(#securityUser)")
  public ResponseEntity<ApiResponse<Long>> createStore(
      @Valid @RequestBody StoreCreateRequest request,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    Long storeId = storeService.createStore(request, securityUser.getCurrentActiveProfile().getId());
    return ResponseEntity.created(URI.create("/api/v1/stores/" + storeId))
        .body(ApiResponse.success(storeId));
  }

  @GetMapping("/{storeId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<StoreResponse>> getStore(
      @PathVariable Long storeId
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.getStore(storeId)));
  }

  @PutMapping("/{storeId}")
  @PreAuthorize("@profileSecurity.isSeller(#securityUser) and @storeSecurity.isOwner(#storeId, #securityUser)")
  public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
      @PathVariable Long storeId,
      @Valid @RequestBody StoreUpdateRequest request,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(storeId, request)));
  }

  @DeleteMapping("/{storeId}")
  @PreAuthorize("@profileSecurity.isSeller(#securityUser) and @storeSecurity.isOwner(#storeId, #securityUser)")
  public ResponseEntity<ApiResponse<Void>> deleteStore(
      @PathVariable Long storeId,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    storeService.deleteStore(storeId);
    return ResponseEntity.status(204).body(ApiResponse.success());
  }

  @PostMapping("/{storeId}/toggle-status")
  @PreAuthorize("@profileSecurity.isSeller(#securityUser) and @storeSecurity.isOwner(#storeId, #securityUser)")
  public ResponseEntity<ApiResponse<StoreResponse>> toggleStoreStatus(
      @PathVariable Long storeId,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    return ResponseEntity.ok(ApiResponse.success(storeService.toggleStoreStatus(storeId)));
  }
}
