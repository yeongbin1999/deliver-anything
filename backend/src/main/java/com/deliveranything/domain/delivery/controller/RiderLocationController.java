package com.deliveranything.domain.delivery.controller;

import com.deliveranything.domain.delivery.dto.RiderLocationDto;
import com.deliveranything.domain.delivery.service.RiderLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RiderLocationController {

  private final RiderLocationService riderLocationService;
  private final SimpMessagingTemplate messagingTemplate;

  // 라이더 클라이언트에서 서버로 [[/app/location]] 으로 전송
  // 서버에서 소비자 클라이언트로  [[/topic/location]] 으로 전송
  @MessageMapping("/location")
  public void updateLocation(@Valid @Payload RiderLocationDto location) {
    riderLocationService.saveRiderLocation(location);
    messagingTemplate.convertAndSend("/topic/location", location);
  }
}
