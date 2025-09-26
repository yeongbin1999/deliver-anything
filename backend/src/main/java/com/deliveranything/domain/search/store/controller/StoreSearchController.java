package com.deliveranything.domain.search.store.controller;

import com.deliveranything.domain.search.store.dto.StoreSearchRequest;
import com.deliveranything.domain.search.store.dto.StoreSearchResponse;
import com.deliveranything.domain.search.store.service.StoreSearchService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search/stores")
public class StoreSearchController {

  private final StoreSearchService storeSearchService;

  @GetMapping
  public ResponseEntity<ApiResponse<CursorPageResponse<StoreSearchResponse>>> searchStores(@Valid @ModelAttribute StoreSearchRequest request) {
    CursorPageResponse<StoreSearchResponse> results = storeSearchService.search(request);

    return ResponseEntity.ok(ApiResponse.success(results));
  }
}