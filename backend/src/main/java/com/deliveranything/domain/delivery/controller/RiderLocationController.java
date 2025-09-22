package com.deliveranything.domain.delivery.controller;

import com.deliveranything.domain.delivery.dto.RiderLocationDto;
import com.deliveranything.domain.delivery.service.RiderLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RiderLocationController {

  private final RiderLocationService riderLocationService;

  @MessageMapping("/location") // 클라이언트에서 /app/location 으로 전송
  public void updateLocation(RiderLocationDto location) {
    riderLocationService.saveRiderLocation(location);
  }
}
