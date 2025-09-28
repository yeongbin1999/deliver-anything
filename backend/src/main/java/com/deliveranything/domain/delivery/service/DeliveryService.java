package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderDecisionRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.delivery.event.event.OrderDeliveryStatusRedisPublisher;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.domain.user.service.RiderProfileService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final RiderProfileService riderProfileService;
  private final OrderDeliveryStatusRedisPublisher orderDeliveryStatusRedisPublisher;

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

  public void publishRiderDecision(@Valid RiderDecisionRequestDto decisionRequestDto,
      Long currentActiveProfileId) {
    DeliveryStatus status = DeliveryStatus.valueOf(decisionRequestDto.decisionStatus());
    OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(
        decisionRequestDto.orderId(), currentActiveProfileId, status);
    orderDeliveryStatusRedisPublisher.publish(event);
  }
}
