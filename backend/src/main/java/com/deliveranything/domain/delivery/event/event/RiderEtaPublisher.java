package com.deliveranything.domain.delivery.event.event;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiderEtaPublisher {

  private final KafkaTemplate<String, RiderNotificationDto> kafkaTemplate;

  public void publish(List<RiderNotificationDto> dtoList) {
    dtoList.forEach(dto -> kafkaTemplate.send("order-events", dto.riderId(), dto));
  }
}
