package com.deliveranything.domain.store.controller;

import com.deliveranything.domain.store.service.StoreService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/stores")
public class StoreController {

  private StoreService storeService;

}
