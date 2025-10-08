package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.OrderDetailsDto;
import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.event.OrderAcceptedEvent;
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

  public Mono<List<RiderNotificationDto>> processOrderEvent(OrderAcceptedEvent orderEvent) {
    double storeLat = orderEvent.storeLat();
    double storeLon = orderEvent.storeLon();
    double customerLat = orderEvent.customerLat();
    double customerLon = orderEvent.customerLon();
    String orderId = orderEvent.orderId();

    // 상점 → 고객 거리 계산
    Mono<Map<String, Double>> distanceKmMono = etaService.getDistance(storeLat, storeLon,
        customerLat, customerLon);

    return distanceKmMono.flatMap(distanceMap -> {
      double distanceKm = distanceMap.getOrDefault("distance", 0.0);
      int expectedCharge = 3000;
      if (distanceKm > 3.0) {
        expectedCharge += (int) Math.ceil((distanceKm - 3.0) * 1000); // 3km 초과 시 추가 요금
      }
      
      // 반경 내 라이더 ETA 조회
      int finalExpectedCharge = expectedCharge;
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
                          .expectedCharge(finalExpectedCharge)
                          .build()
                  )
                  .riderId(riderId)
                  .etaMinutes(etaMinutes)
                  .orderDeliveryStatus(OrderStatus.RIDER_ASSIGNED)
                  .build();
              dtoList.add(dto);
            });
            return dtoList;
          });
    });
  }
}
