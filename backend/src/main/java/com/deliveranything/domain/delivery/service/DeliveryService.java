package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderDecisionRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.enums.RiderToggleStatus;
import com.deliveranything.domain.user.profile.service.RiderProfileService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final RiderProfileService riderProfileService;
  private final DeliveryRepository deliveryRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RedisTemplate<String, Object> redisTemplate;

  public void updateRiderStatus(Long riderId, RiderToggleStatusRequestDto riderStatusRequestDto) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderId);

    riderProfile.setToggleStatus(RiderToggleStatus.fromString(riderStatusRequestDto.riderStatus()));
  }

  public void updateDeliveryArea(Long riderId, DeliveryAreaRequestDto deliveryAreaRequestDto) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderId);

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

    eventPublisher.publishEvent(event);
  }

  // 라이더 배달 수락/거절 처리 및 상태 이벤트 발행
  public void publishRiderDecision(@Valid RiderDecisionRequestDto decisionRequestDto,
      Long currentActiveProfileId) {
    DeliveryStatus status = DeliveryStatus.valueOf(decisionRequestDto.decisionStatus());

    // 이벤트만 발행 - 실제 상태 변경은 구독자에서 처리
    OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(
        decisionRequestDto.orderId(), currentActiveProfileId, status);
    eventPublisher.publishEvent(event);
  }

  // Delivery 생성
  public Delivery createDelivery(Order order, Long riderProfileId) {
    return Delivery.builder()
        .expectedTime(0) // 추후 ETA 계산 로직으로 추가 예정
        .requested(order.getRiderNote())
        .status(com.deliveranything.domain.delivery.enums.DeliveryStatus.RIDER_ASSIGNED)
        .charge(0)  // 추후 배달료 계산 로직으로 추가 예정
        .store(order.getStore())
        .customer(order.getCustomer())
        .riderProfile(riderProfileService.getRiderProfileById(riderProfileId))
        .build();
  }
}
