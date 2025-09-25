package com.deliveranything.domain.product.product.controller;

import com.deliveranything.domain.product.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;


}
