package com.deliveranything.domain.store.categoty.controller;

import com.deliveranything.domain.store.categoty.service.StoreCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/store-categories")
public class StoreCategoryController {

  private  final StoreCategoryService storeCategoryService;

}
