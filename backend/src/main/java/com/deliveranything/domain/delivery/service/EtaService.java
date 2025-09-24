package com.deliveranything.domain.delivery.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EtaService {

  private final WebClient osrmWebClient;

  // OSRM Table API 호출 (Reactive)
  public Mono<Map<String, Double>> getEtaForMultipleReactive(
      // double storeLat, double storeLon,
      double userLat, double userLon,
      List<Point> riderPoints,
      List<String> riderIds
  ) {
    StringBuilder coordinates = new StringBuilder();
    coordinates.append(userLon).append(",").append(userLat); // 0번 → 고객 좌표

    for (Point p : riderPoints) {
      coordinates.append(";").append(p.getX()).append(",").append(p.getY());
    }

    return osrmWebClient.get()
        .uri("/table/v1/driving/{coordinates}?annotations=duration", coordinates)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        })
        .map(response -> {
          // 타입 캐스팅 예외 주의 및 처리 필요 -> 예정
          List<List<Double>> durations = (List<List<Double>>) response.get("durations");
          Map<String, Double> etaMap = new HashMap<>();
          for (int i = 0; i < riderIds.size(); i++) {
            double etaSec = durations.get(0).get(i + 1);
            etaMap.put(riderIds.get(i), etaSec / 60.0); // 분 단위 변환
          }
          return etaMap;
        });
  }

  // 상점 <-> 주문자 사이 거리 (eta 기준)
  public Mono<Map<String, Double>> getDistance(
      double storeLat, double storeLon,
      double userLat, double userLon
  ) {
    String coordinates = String.format("%f,%f;%f,%f", storeLon, storeLat, userLon, userLat);

    return osrmWebClient.get()
        .uri("/table/v1/driving/{coordinates}?annotations=distance", coordinates)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        })
        .map(response -> {
          List<List<Double>> distances = (List<List<Double>>) response.get("distances");
          Map<String, Double> result = new HashMap<>();

          if (distances != null && !distances.isEmpty()) {
            double distanceM = distances.get(0).get(1);  // 상점 → 주문자 거리(m)
            // 소수 둘째 자리에서 반올림
            double distanceKm = Math.round(
                ((distanceM / 1000.0) * 100.0) / 100); // km 단위 변환 (소수 둘째자리 반올림)
            result.put("distance", distanceKm);
          }

          return result;
        });
  }
}
