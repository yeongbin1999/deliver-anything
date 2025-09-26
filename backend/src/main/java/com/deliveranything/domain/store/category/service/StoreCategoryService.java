package com.deliveranything.domain.store.category.service;

import com.deliveranything.domain.store.category.dto.StoreCategoryResponse;
import com.deliveranything.domain.store.category.entity.StoreCategory;
import com.deliveranything.domain.store.category.repository.StoreCategoryRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {

  private final StoreCategoryRepository storeCategoryRepository;

  public List<StoreCategoryResponse> getAllCategories() {
    return storeCategoryRepository.findAll().stream()
        .map(c -> new StoreCategoryResponse(c.getId(), c.getName()))
        .toList();
  }

  public StoreCategory findById(Long categoryId) {
    return storeCategoryRepository.findById(categoryId)
        .orElseThrow(() -> new CustomException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
  }
}
