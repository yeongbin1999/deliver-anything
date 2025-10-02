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
import com.deliveranything.domain.user.service.CustomerProfileService;
import com.deliveranything.domain.user.service.RiderProfileService;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
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
    riderProfile.updateToggleStatus(riderStatusRequestDto.riderStatus());
  }

  public void updateDeliveryArea(Long riderId, DeliveryAreaRequestDto deliveryAreaRequestDto) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderId);
    riderProfile.updateDeliveryArea(deliveryAreaRequestDto.deliveryArea());
  }

  public void changeDeliveryStatus(Long deliveryId, DeliveryStatus next) {
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
  // 페이지네이션 까지 필요 없음 -> List 반환
  public List<CurrentDeliveringResponseDto> getCurrentDeliveringInfo(Long riderProfileId) {
    List<Delivery> currentDeliveries = deliveryRepository.findByRiderProfileIdAndStatus(
        riderProfileId, DeliveryStatus.IN_PROGRESS);

    return currentDeliveries.stream()
        .map(delivery -> {
          Order currentOrder = orderService.getCurrentOrderByDeliveryId(delivery.getId());
          return CurrentDeliveringResponseDto.builder()
              .orderId(delivery.getId())
              .deliveryId(delivery.getId())
              .storeName(delivery.getStore().getName())
              .customerAddress(currentOrder.getAddress())
              .remainingTime(getRemainingTime(delivery))
              .build();
        })
        .toList();
  }

  // 진행 중인 배달 정보 상세 조회
  public CurrentDeliveringDetailsDto getCurrentDeliveringDetails(Long deliveryId) {
    Delivery currentDelivery = deliveryRepository.findById(deliveryId)
        .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

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
  public DeliveredSummaryResponseDto getDeliveredSummary(
      Long riderProfileId,
      String cursor,
      Integer size
  ) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderProfileId);

    // 페이징된 배달 내역 조회
    CursorPageResponse<DeliveredDetailsDto> deliveredDetails =
        getDeliveredDetailsCursor(riderProfileId, cursor, size != null ? size : 10);

    return DeliveredSummaryResponseDto.builder()
        .thisWeekDeliveredCount(getThisWeekCompletedCount(riderProfileId))
        .waitingSettlementAmount(0) // TODO: 정산 도메인 구현 후 수정
        .completedSettlementAmount(0) // TODO: 정산 도메인 구현 후 수정
        .deliveredDetails(deliveredDetails)
        .build();
  }

  // 배달 완료 내역 커서 페이징 조회
  private CursorPageResponse<DeliveredDetailsDto> getDeliveredDetailsCursor(
      Long riderProfileId,
      String nextPageToken,
      int size
  ) {
    // 커서 디코딩
    LocalDateTime lastCompletedAt = null;
    Long lastOrderId = null;

    if (nextPageToken != null) {
      String[] decoded = CursorUtil.decode(nextPageToken);

      if (decoded != null && decoded.length == 2) {
        try {
          lastCompletedAt = LocalDateTime.parse(decoded[0]);
          lastOrderId = Long.parseLong(decoded[1]);
        } catch (Exception e) {
          lastCompletedAt = null;
          lastOrderId = null;
        }
      }
    }

    final LocalDateTime finalLastCompletedAt = lastCompletedAt;
    final Long finalLastOrderId = lastOrderId;

    // 완료된 배달 조회 (size + 1개 조회하여 hasNext 판단)
    List<Delivery> allCompletedDeliveries = deliveryRepository
        .findByRiderProfileIdAndStatus(riderProfileId, DeliveryStatus.COMPLETED);

    // 정렬 및 필터링
    List<Delivery> filteredDeliveries = allCompletedDeliveries.stream()
        .sorted((d1, d2) -> {
          // 1차: completedAt 기준 내림차순 (최신순)
          int dateCompare = d2.getCompletedAt().compareTo(d1.getCompletedAt());
          if (dateCompare != 0) {
            return dateCompare;
          }
          // 2차: id 기준 내림차순 (동일 시간일 경우 큰 ID가 먼저)
          return Long.compare(d2.getId(), d1.getId());
        })
        .filter(d -> {
          if (finalLastCompletedAt == null) {
            return true;
          }
          int compareDate = d.getCompletedAt().compareTo(finalLastCompletedAt);
          // 더 이전 날짜
          if (compareDate < 0) {
            return true;
          }
          if (compareDate == 0 && d.getId() < finalLastOrderId) {
            return true; // 같은 날짜, 더 작은 ID
          }
          return false;
        })
        .limit(size + 1) // hasNext 판단용 1개 추가
        .toList();

    // hasNext 판단
    boolean hasNext = filteredDeliveries.size() > size;
    List<Delivery> pageDeliveries = hasNext ?
        filteredDeliveries.subList(0, size) : filteredDeliveries;

    // DTO 변환
    List<DeliveredDetailsDto> deliveredDetailsList = pageDeliveries.stream()
        .map(delivery -> {
          OrderResponse order = orderService.getOrderByDeliveryId(delivery.getId());
          return DeliveredDetailsDto.builder()
              .orderId(order.id())
              .storeName(order.storeName())
              .completedAt(delivery.getCompletedAt())
              .customerAddress(order.address())
              .settlementStatus("PENDING") // TODO: 정산 도메인 구현 후 수정
              .deliveryCharge(delivery.getCharge())
              .build();
        })
        .toList();

    // 다음 페이지 토큰 생성
    if (hasNext && !deliveredDetailsList.isEmpty()) {
      DeliveredDetailsDto last = deliveredDetailsList.get(deliveredDetailsList.size() - 1);
      nextPageToken = CursorUtil.encode(last.completedAt(), last.orderId());
    }

    return new CursorPageResponse<>(
        deliveredDetailsList,
        nextPageToken,
        hasNext
    );
  }

  // === 편의 메서드 ===

  public Delivery getInProgressDeliveryByRiderId(Long riderProfileId) {
    return deliveryRepository.findFirstByRiderProfileIdAndStatusOrderByStartedAtDesc(
            riderProfileId, DeliveryStatus.IN_PROGRESS)
        .orElseThrow(() -> new CustomException(ErrorCode.NO_ACTIVE_DELIVERY));
  }

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