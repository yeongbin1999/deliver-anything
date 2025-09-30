package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.event.redis.DeliveryStatusRedisPublisher;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.domain.user.service.RiderProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final RiderProfileService riderProfileService;
  private final DeliveryRepository deliveryRepository;
  private final DeliveryStatusRedisPublisher deliveryStatusRedisPublisher;
  private final RedisTemplate<String, Object> redisTemplate;

  public void updateRiderStatus(RiderToggleStatusRequestDto riderStatusRequestDto) {
    // 라이더 상태 업데이트 로직 구현
    // 추후 JWT에서 사용자 정보-Profile ID가 넘어오면 파라미터 변경 예정
    RiderProfile riderProfile = riderProfileService
        .getRiderProfileById(riderStatusRequestDto.riderProfileId());

    riderProfile.setToggleStatus(RiderToggleStatus.fromString(riderStatusRequestDto.riderStatus()));
  }

  public void updateDeliveryArea(DeliveryAreaRequestDto deliveryAreaRequestDto) {
    // 배달 가능 지역 설정 로직 구현
    // 추후 JWT에서 사용자 정보-Profile ID가 넘어오면 파라마터 변경 예정
    RiderProfile riderProfile = riderProfileService
        .getRiderProfileById(deliveryAreaRequestDto.riderProfileId());

    riderProfile.setDeliveryArea(deliveryAreaRequestDto.deliveryArea());
  }

  public void changeStatus(Long deliveryId, DeliveryStatus next) {
    Delivery delivery = deliveryRepository.findById(deliveryId)
        .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

    // 이벤트만 발행 - 실제 상태 변경은 구독자에서 처리
    DeliveryStatusEvent event = DeliveryStatusEvent.builder()
        .deliveryId(delivery.getId())
        .orderId(delivery.getStore().getId())
        .riderProfileId(delivery.getRiderProfile().getId())
        .customerProfileId(delivery.getCustomer().getId())
        .sellerProfileId(delivery.getStore().getSellerProfileId())
        .status(delivery.getStatus())
        .occurredAtEpochMs(System.currentTimeMillis())
        .nextStatus(next)
        .build();

    deliveryStatusRedisPublisher.publish(event);
  }
}
