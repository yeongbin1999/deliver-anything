package com.deliveranything.domain.store.store.service;

import com.deliveranything.domain.store.store.dto.StoreCategoryResponse;
import com.deliveranything.domain.store.store.enums.StoreCategoryType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {

  public List<StoreCategoryResponse> getAllCategories() {
    return Arrays.stream(StoreCategoryType.values())
        .sorted(Comparator.comparingInt(StoreCategoryType::getOrder))
        .map(c -> new StoreCategoryResponse(c.name(), c.getDisplayName(), c.getOrder()))
        .toList();
  }
}
