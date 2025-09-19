package com.deliveranything.domain.store.service;

import com.deliveranything.domain.store.repository.StoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {

  private final StoreCategoryRepository storeCategoryRepository;

}
