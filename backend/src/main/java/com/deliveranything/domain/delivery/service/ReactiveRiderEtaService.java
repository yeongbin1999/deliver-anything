package com.deliveranything.domain.delivery.service;

import static com.deliveranything.domain.delivery.service.RiderLocationService.RIDER_GEO_KEY;

import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.enums.RiderToggleStatus;
import com.deliveranything.domain.user.profile.rerpository.RiderProfileRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveRiderEtaService {

  private final StringRedisTemplate redisTemplate;
  private final EtaService etaService;
  private final RiderProfileRepository riderProfileRepository;

  //반경 내 라이더 검색 후 ETA 계산
  // Kafka 발행(2차 구현)
  public Mono<Map<String, Double>> findNearbyRidersEta(double customerLat, double customerLon,
      double radiusKm) {
    // Redis GEOSEARCH
    GeoResults<RedisGeoCommands.GeoLocation<String>> nearbyRiders =
        redisTemplate.opsForGeo().search(
            RIDER_GEO_KEY,
            GeoReference.fromCoordinate(customerLat, customerLon),
            new Distance(radiusKm, Metrics.KILOMETERS),
            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance()
        );

    if (nearbyRiders == null || nearbyRiders.getContent().isEmpty()) {
      return Mono.empty();
    }

    List<String> riderIds = new ArrayList<>();
    List<Point> riderPoints = new ArrayList<>();

    for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : nearbyRiders) {
      RedisGeoCommands.GeoLocation<String> loc = result.getContent();
      RiderProfile riderProfile = riderProfileRepository.findById(Long.parseLong(loc.getName()))
          .orElseThrow(() -> new CustomException(ErrorCode.RIDER_NOT_FOUND));
      if (riderProfile.getToggleStatus() == RiderToggleStatus.OFF) {
        continue; // OFF 상태 라이더는 제외
      }
      riderIds.add(loc.getName());
      riderPoints.add(loc.getPoint());
    }

    // OSRM Table API → ETA 계산 (비동기)
    return etaService.getEtaForMultipleReactive(customerLat, customerLon, riderPoints, riderIds);
  }
}
