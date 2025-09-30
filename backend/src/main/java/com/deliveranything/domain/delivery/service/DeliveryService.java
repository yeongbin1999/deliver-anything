package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderDecisionRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.dto.response.CurrentDeliveringDetailsDto;
import com.deliveranything.domain.delivery.dto.response.CurrentDeliveringResponseDto;
import com.deliveranything.domain.delivery.dto.response.DeliveredDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliveredSummaryResponseDto;
import com.deliveranything.domain.delivery.dto.response.DeliveringCustomerDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliveringStoreDetailsDto;
import com.deliveranything.domain.delivery.dto.response.TodayDeliveringResponseDto;
import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.delivery.event.event.redis.DeliveryStatusRedisPublisher;
import com.deliveranything.domain.delivery.event.event.redis.OrderDeliveryStatusRedisPublisher;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.entity.profile.SellerProfile;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.domain.user.service.CustomerProfileService;
import com.deliveranything.domain.user.service.RiderProfileService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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
  private final OrderService orderService;
  private final StoreService storeService;
  private final ApplicationEventPublisher eventPublisher;
  private final CustomerProfileService customerProfileService;

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
        .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

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
        decisionRequestDto.orderId(), currentActiveProfileId, status,
        decisionRequestDto.etaMinutes());
    eventPublisher.publishEvent(event);
  }

  // Delivery 생성
  public Delivery createDelivery(Order order, Long riderProfileId, Double eta) {
    return Delivery.builder()
        .expectedTime(eta)
        .requested(order.getRiderNote())
        .status(com.deliveranything.domain.delivery.enums.DeliveryStatus.RIDER_ASSIGNED)
        .charge(0)  // 추후 배달료 계산 로직으로 추가 예정
        .store(order.getStore())
        .customer(order.getCustomer())
        .riderProfile(riderProfileService.getRiderProfileById(riderProfileId))
        .build();
  }

  // 오늘 라이더의 작업 내역 조회
  public TodayDeliveringResponseDto getTodayDeliveringInfo(Long riderProfileId) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderProfileId);

    return TodayDeliveringResponseDto.builder()
        .now(LocalDateTime.now())
        .currentStatus(riderProfile.getToggleStatus())
        .todayDeliveryCount(getTodayCompletedCountByRider(riderProfileId))
        .todayEarningAmount(getTodayEarningAmountByRiderId(riderProfileId))
        .avgDeliveryTime(getAvgDeliveryTimeByRiderId(riderProfileId))
        .build();
  }

  // 진행 중인 배달 정보 조회
  public CurrentDeliveringResponseDto getCurrentDeliveringInfo(Long riderProfileId) {
    Delivery currentDelivery = deliveryRepository.findByRiderProfileIdAndStatus(
            riderProfileId, DeliveryStatus.IN_PROGRESS)
        .orElseThrow(() -> new CustomException(ErrorCode.NO_ACTIVE_DELIVERY));

    Order currentOrder = orderService.getCurrentOrderByDeliveryId(currentDelivery.getId());

    return CurrentDeliveringResponseDto.builder()
        .orderId(currentDelivery.getId())
        .storeName(currentDelivery.getStore().getName())
        .customerAddress(currentOrder.getAddress())
        .remainingTime(getRemainingTime(currentDelivery))
        .build();
  }

  // 진행 중인 배달 정보 상세 조회
  public CurrentDeliveringDetailsDto getCurrentDeliveringDetails(Long riderProfileId) {
    Delivery currentDelivery = deliveryRepository.findByRiderProfileIdAndStatus(
            riderProfileId, DeliveryStatus.IN_PROGRESS)
        .orElseThrow(() -> new CustomException(ErrorCode.NO_ACTIVE_DELIVERY));

    OrderResponse currentOrder = orderService.getOrderByDeliveryId(currentDelivery.getId());
    Store currentStore = currentDelivery.getStore();
    SellerProfile sellerProfile = storeService.getSellerProfileById(
        currentStore.getSellerProfileId());
    CustomerProfile customerProfile = currentDelivery.getCustomer();

    return CurrentDeliveringDetailsDto.builder()
        .orderId(currentDelivery.getId())
        .storeDetails(
            DeliveringStoreDetailsDto.builder()
                .storeName(currentOrder.storeName())
                .storeRoadAddress(currentStore.getRoadAddr())
                .sellerBusinessPhoneNumber(sellerProfile.getBusinessPhoneNumber())
                .build()
        )
        .customerDetails(
            DeliveringCustomerDetailsDto.builder()
                .customerNickname(customerProfile.getNickname())
                .customerAddress(getCustomerDefaultAddress(customerProfile.getDefaultAddressId()))
                .customerPhoneNumber(customerProfile.getCustomerPhoneNumber())
                .riderNote(currentOrder.riderNote())
                .build()
        )
        .remainingTime(getRemainingTime(currentDelivery))
        .expectedTime(currentDelivery.getExpectedTime())
        .build();
  }

  // 총 배달 내역 요약 조회 + 배달 완료 리스트 조회
  public DeliveredSummaryResponseDto getDeliveredSummary(Long riderProfileId) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderProfileId);

    List<Delivery> completedDeliveries = deliveryRepository
        .findByRiderProfileId(riderProfileId);

    // Delivery → DeliveredDetailsDto 변환
    List<DeliveredDetailsDto> deliveredDetails = completedDeliveries.stream()
        .map(d -> DeliveredDetailsDto.builder()
            .completedAt(d.getCompletedAt())
            .storeName(d.getStore().getName())
            .orderId(d.getId()) // 또는 실제 Order ID 조회
            .customerAddress(getCustomerDefaultAddress(d.getCustomer().getDefaultAddressId()))
            .settlementStatus("PENDING") // TODO: 정산 도메인 구현 후 실제 상태로 변경
            .deliveryCharge(d.getCharge())
            .build())
        .toList();

    return DeliveredSummaryResponseDto.builder()
        .thisWeekDeliveredCount(getThisWeekCompletedCount(riderProfileId))
        .totalDeliveryCharges(getTotalDeliveryCharges(riderProfileId))
        .waitingSettlementAmount(0) // TODO: 정산 도메인 구현 후 수정
        .completedSettlementAmount(0) // TODO: 정산 도메인 구현 후 수정
        .deliveredDetails(deliveredDetails)
        .build();
  }

  // === 편의 메서드 ===

  // 오늘 배달 건 수
  public Long getTodayCompletedCountByRider(Long riderProfileId) {
    return deliveryRepository.countTodayCompletedDeliveriesByRider(riderProfileId);
  }

  // 이번 주 배달 건 수
  public Long getThisWeekCompletedCount(Long riderProfileId) {
    // 이번 주 시작일 계산 (월요일 기준)
    LocalDateTime weekStart = LocalDateTime.now()
        .with(DayOfWeek.MONDAY)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0);

    return deliveryRepository.countThisWeekCompletedDeliveriesByRiderProfileId(riderProfileId,
        weekStart);
  }

  // 오늘 정산 금액 합계
  public Integer getTodayEarningAmountByRiderId(Long riderProfileId) {
//    return settlementRepository.sumTodayEarningsByRider(riderProfileId);
    return 0; // TODO: 정산 도메인 구현 후 수정
  }

  // 평균 배달 시간 (분 단위, 소수점 첫째 자리까지)
  public Double getAvgDeliveryTimeByRiderId(Long riderProfileId) {
    List<Delivery> completedDeliveries = deliveryRepository
        .findTodayCompletedDeliveriesByRider(riderProfileId);

    double avgMinutes = completedDeliveries.stream()
        .filter(d -> d.getStartedAt() != null && d.getCompletedAt() != null)
        .mapToDouble(d ->
            Duration.between(d.getStartedAt(), d.getCompletedAt()).getSeconds() / 60.0)
        .average()
        .orElse(0.0);

    return Math.round(avgMinutes * 10.0) / 10.0;
  }

  // 남은 예상 시간 계산 (분 단위) -> 지연될 경우 - eta보다 더 걸릴 경우 (0분)으로 표시
  private static double getRemainingTime(Delivery currentDelivery) {
    double remainingTime = 0.0;
    if (currentDelivery.getStartedAt() != null && currentDelivery.getExpectedTime() != null) {
      long elapsedMinutes = Duration.between(currentDelivery.getStartedAt(), LocalDateTime.now())
          .toMinutes();
      remainingTime = Math.max(0, currentDelivery.getExpectedTime() - elapsedMinutes);
    }
    return remainingTime;
  }

  // 고객 기본 주소 조회
  private String getCustomerDefaultAddress(Long defaultAddressId) {
    return customerProfileService.getCurrentAddress(defaultAddressId).getAddress();
  }

  private Long getTotalDeliveryCharges(Long riderProfileId) {
    return deliveryRepository.sumTotalDeliveryChargesByRider(riderProfileId);
  }
}
