package com.deliveranything.domain.store.categoty.service;

import com.deliveranything.domain.store.categoty.repository.StoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {

  private final StoreCategoryRepository storeCategoryRepository;

}
