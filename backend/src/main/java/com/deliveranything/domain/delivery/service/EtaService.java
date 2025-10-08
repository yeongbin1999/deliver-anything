package com.deliveranything.domain.delivery.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EtaService {

  private final WebClient.Builder webClientBuilder;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  private static final String KAKAO_BASE_URL = "https://apis-navi.kakaomobility.com/v1";

  // Kakao Map API 호출 (Reactive)
  public Mono<Map<String, Double>> getEtaForMultipleReactive(
      // double storeLat, double storeLon,
      double userLat, double userLon,
      List<Point> riderPoints,
      List<String> riderIds
  ) {
    WebClient webClient = webClientBuilder.baseUrl(KAKAO_BASE_URL).build();

    return Flux.fromIterable(riderIds)
        .index() // (index, riderId)
        .flatMap(tuple -> {
          long idx = tuple.getT1();
          String riderId = tuple.getT2();
          Point riderPoint = riderPoints.get((int) idx);

          return webClient.get()
              .uri(uriBuilder -> uriBuilder
                  .path("/directions")
                  .queryParam("origin", riderPoint.getX() + "," + riderPoint.getY()) // lon,lat
                  .queryParam("destination", userLon + "," + userLat)
                  .build())
              .header("Authorization", "KakaoAK " + kakaoApiKey)
              .retrieve()
              .bodyToMono(Map.class)
              .map(response -> {
                Map<String, Object> routes
                    = (Map<String, Object>) ((List<?>) response.get("routes")).get(0);
                Map<String, Object> summary = (Map<String, Object>) routes.get("summary");
                Double duration = ((Number) summary.get("duration")).doubleValue(); // 초 단위
                return Map.entry(riderId, duration / 60.0); // 분 단위 변환
              });
        })
        .collectMap(Map.Entry::getKey, Map.Entry::getValue); // Map<String, Double>
  }

  // 상점 <-> 주문자 사이 거리 (eta 기준)
  public Mono<Map<String, Double>> getDistance(
      double storeLat, double storeLon,
      double userLat, double userLon
  ) {
    WebClient webClient = webClientBuilder.baseUrl(KAKAO_BASE_URL).build();

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/directions")
            .queryParam("origin", storeLon + "," + storeLat)
            .queryParam("destination", userLon + "," + userLat)
            .build())
        .header("Authorization", "KakaoAK " + kakaoApiKey)
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> {
          Map<String, Object> routes = (Map<String, Object>) ((List<?>) response.get("routes")).get(
              0);
          Map<String, Object> summary = (Map<String, Object>) routes.get("summary");
          Double distanceM = ((Number) summary.get("distance")).doubleValue(); // m 단위
          double distanceKm =
              Math.round((distanceM / 1000.0) * 100.0) / 100.0; // km 단위, 소수 둘째 자리 반올림

          Map<String, Double> result = new HashMap<>();
          result.put("distance", distanceKm);
          return result;
        });
  }
}
