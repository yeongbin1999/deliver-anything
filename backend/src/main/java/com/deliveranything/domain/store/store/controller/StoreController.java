package com.deliveranything.domain.store.store.controller;

import com.deliveranything.domain.store.store.dto.StoreSearchRequest;
import com.deliveranything.domain.store.store.dto.StoreSliceResponse;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stores")
public class StoreController {

  private final StoreService storeService;

  @GetMapping
  public ApiResponse<StoreSliceResponse> searchStoresByDistance(
      @ModelAttribute StoreSearchRequest request) {
    return ApiResponse.success(storeService.searchByDistance(request));
  }
}
