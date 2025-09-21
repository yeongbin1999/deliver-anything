package com.deliveranything.domain.store.store.controller;

import com.deliveranything.domain.store.store.dto.StoreCategoryResponse;
import com.deliveranything.domain.store.store.service.StoreCategoryService;
import com.deliveranything.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/store-categories")
public class StoreCategoryController {

  private final StoreCategoryService storeCategoryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<StoreCategoryResponse>>> getCategories() {
    return ResponseEntity.ok(ApiResponse.success(storeCategoryService.getAllCategories()));
  }
}
