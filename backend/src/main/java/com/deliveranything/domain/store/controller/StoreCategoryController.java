package com.deliveranything.domain.store.controller;

import com.deliveranything.domain.store.service.StoreCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/store-categories")
public class StoreCategoryController {

  private  final StoreCategoryService storeCategoryService;

}
