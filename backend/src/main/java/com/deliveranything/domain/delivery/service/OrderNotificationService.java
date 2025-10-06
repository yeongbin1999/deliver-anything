package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.OrderDetailsDto;
import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderDeliveryCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

  private final ReactiveRiderEtaService reactiveRiderEtaService;
  private final EtaService etaService;

  public Mono<List<RiderNotificationDto>> processOrderEvent(OrderDeliveryCreatedEvent orderEvent) {
    double storeLat = orderEvent.storeLat();
    double storeLon = orderEvent.storeLon();
    double customerLat = orderEvent.customerLat();
    double customerLon = orderEvent.customerLon();
    String orderId = orderEvent.orderId();

    // 1️⃣ 상점 → 고객 거리 계산
    Mono<Map<String, Double>> distanceKmMono = etaService.getDistance(storeLat, storeLon,
        customerLat, customerLon);

    return distanceKmMono.flatMap(distanceMap -> {
      double distanceKm = distanceMap.getOrDefault("distance", 0.0);
      int expectedCharge = 1000 + (int) (distanceKm * 500); // 임시 배달료 계산

      // 2️⃣ 반경 내 라이더 ETA 조회
      return reactiveRiderEtaService.findNearbyRidersEta(customerLat, customerLon, 3.0)
          .map(etaMap -> {
            List<RiderNotificationDto> dtoList = new ArrayList<>();
            etaMap.forEach((riderId, etaMinutes) -> {
              RiderNotificationDto dto = RiderNotificationDto.builder()
                  .orderDetailsDto(
                      OrderDetailsDto.builder()
                          .orderId(orderId)
                          .storeName(orderEvent.storeName())
                          .distance(distanceKm)
                          .expectedCharge(expectedCharge)
                          .build()
                  )
                  .riderId(riderId)
                  .etaMinutes(etaMinutes)
                  .orderDeliveryStatus(OrderStatus.RIDER_ASSIGNED)
                  .build();
              dtoList.add(dto);
            });
            return dtoList; // Kafka 발행은 여기서 하지 않음
          });
    });
  }
}
