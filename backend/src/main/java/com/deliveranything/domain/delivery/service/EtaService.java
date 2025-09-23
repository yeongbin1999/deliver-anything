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
          List<List<Double>> durations = (List<List<Double>>) response.get("durations");
          Map<String, Double> etaMap = new HashMap<>();
          for (int i = 0; i < riderIds.size(); i++) {
            double etaSec = durations.get(0).get(i + 1);
            etaMap.put(riderIds.get(i), etaSec / 60.0); // 분 단위 변환
          }
          return etaMap;
        });
  }
}
