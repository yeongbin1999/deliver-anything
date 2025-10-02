package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderDecisionRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.dto.response.CurrentDeliveringDetailsDto;
import com.deliveranything.domain.delivery.dto.response.CurrentDeliveringResponseDto;
import com.deliveranything.domain.delivery.dto.response.DeliveredDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliveredSettlementDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliveredSummaryResponseDto;
import com.deliveranything.domain.delivery.dto.response.DeliveringCustomerDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliveringStoreDetailsDto;
import com.deliveranything.domain.delivery.dto.response.DeliverySettlementResponseDto;
import com.deliveranything.domain.delivery.dto.response.TodayDeliveringResponseDto;
import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.delivery.repository.DeliveryRepository;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.settlement.dto.SettlementResponse;
import com.deliveranything.domain.settlement.service.SettlementBatchService;
import com.deliveranything.domain.settlement.service.SettlementDetailService;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.entity.profile.SellerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.service.CustomerProfileService;
import com.deliveranything.domain.user.service.RiderProfileService;
import com.deliveranything.domain.user.service.SellerProfileService;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final RiderProfileService riderProfileService;
  private final DeliveryRepository deliveryRepository;
  private final OrderService orderService;
  private final SellerProfileService sellerProfileService;
  private final ApplicationEventPublisher eventPublisher;
  private final CustomerProfileService customerProfileService;
  private final SettlementBatchService settlementBatchService;
  private final SettlementDetailService settlementDetailService;

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
    // JOIN FETCH로 Store와 Order를 한 번에 가져옴
    List<Delivery> currentDeliveries = deliveryRepository.findByRiderProfileIdAndStatus(
        riderProfileId, DeliveryStatus.IN_PROGRESS);

    return currentDeliveries.stream()
        .map(delivery -> {
          OrderResponse currentOrder = orderService.getOrderByDeliveryId(
              delivery.getId());  // 이미 JOIN FETCH로 가져옴
          return CurrentDeliveringResponseDto.builder()
              .orderId(delivery.getId())
              .deliveryId(delivery.getId())
              .storeName(delivery.getStore().getName())  // 이미 JOIN FETCH로 가져옴
              .customerAddress(currentOrder.address())
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
    SellerProfile sellerProfile = sellerProfileService.getSellerProfileById(
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
      String filter,
      String cursor,
      Integer size
  ) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderProfileId);

    // 페이징된 배달 내역 조회
    CursorPageResponse<DeliveredDetailsDto> deliveredDetails =
        getDeliveredDetailsCursor(riderProfileId, filter, cursor, size != null ? size : 10);

    return DeliveredSummaryResponseDto.builder()
        .thisWeekDeliveredCount(getThisWeekCompletedCount(riderProfileId))
        .waitingSettlementAmount(getTodayEarningAmountByRiderId(riderProfileId))
        .completedSettlementAmount(getThisWeekEarningAmountByRiderId(riderProfileId))
        .deliveredDetails(deliveredDetails)
        .build();
  }

  // 배달 완료 내역 커서 페이징 조회
  private CursorPageResponse<DeliveredDetailsDto> getDeliveredDetailsCursor(
      Long riderProfileId,
      String filter,
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

    // 정렬 결정 (filter에 따라 LATEST 또는 OLDEST)
    boolean isLatest = filter == null || "LATEST".equalsIgnoreCase(filter);

    // 정렬 및 필터링
    List<Delivery> filteredDeliveries = allCompletedDeliveries.stream()
        .sorted((d1, d2) -> {
          // 1차: completedAt 기준 정렬 (LATEST: 내림차순, OLDEST: 오름차순)
          int dateCompare = isLatest
              ? d2.getCompletedAt().compareTo(d1.getCompletedAt())
              : d1.getCompletedAt().compareTo(d2.getCompletedAt());
          if (dateCompare != 0) {
            return dateCompare;
          }
          // 2차: id 기준 정렬
          return isLatest
              ? Long.compare(d2.getId(), d1.getId())
              : Long.compare(d1.getId(), d2.getId());
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

    // DTO 변환 (이미 JOIN FETCH로 Store와 Order를 가져왔으므로 추가 쿼리 없음)
    List<DeliveredDetailsDto> deliveredDetailsList = pageDeliveries.stream()
        .map(delivery -> {
          OrderResponse order = orderService.getOrderByDeliveryId(delivery.getId());
          return DeliveredDetailsDto.builder()
              .orderId(order.id())
              .storeName(delivery.getStore().getName())
              .completedAt(delivery.getCompletedAt())
              .customerAddress(order.address())
              .settlementStatus(getCompletedDeliverySettlementStatus(order.id()))
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

  // 정산 내역 페이지 조회
  public DeliverySettlementResponseDto getDeliverySettlementInfo(
      Long riderProfileId,
      String filter,
      String cursor,
      Integer size
  ) {
    RiderProfile riderProfile = riderProfileService.getRiderProfileById(riderProfileId);

    CursorPageResponse<DeliveredSettlementDetailsDto> deliveredSettlementDetails =
        getDeliveredSettlementDetailsCursor(riderProfileId, filter, cursor, size);

    return DeliverySettlementResponseDto.builder()
        .totalDeliveredCount(getTotalDeliveredCount(riderProfileId))
        .thisWeekTotalEarnings(getThisWeekEarningAmountByRiderId(riderProfileId))
        .thisMonthTotalEarnings(getThisMonthEarningAmountByRiderId(riderProfileId))
        .pendingSettlementAmount(getTodayEarningAmountByRiderId(riderProfileId)) //
        .totalEarnings(getTotalEarnings(riderProfileId))
        .deliveredSettlementDetails(deliveredSettlementDetails)
        .build();
  }

  // 정산 내역 커서 페이징 조회
  private CursorPageResponse<DeliveredSettlementDetailsDto> getDeliveredSettlementDetailsCursor(
      Long riderProfileId,
      String filter,
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

    final LocalDate finalLastSettlementDate =
        lastCompletedAt != null ? lastCompletedAt.toLocalDate() : null;
    final Long finalLastSettlementId = lastOrderId;

    List<SettlementResponse> settlements = null;
    if (filter.equals("WEEK")) {
      settlements = settlementBatchService.getRiderWeekSettlementBatches(riderProfileId);
    } else if (filter.equals("MONTH")) {
      settlements = settlementBatchService.getRiderMonthSettlementBatches(riderProfileId);
    }

    // 정렬 및 필터링
    List<SettlementResponse> filteredSettlement = settlements.stream()
        .sorted((s1, s2) -> {
          // 1차: settlementDate 기준 내림차순 (최신순)
          int dateCompare = s2.settlementDate().compareTo(s1.settlementDate());
          if (dateCompare != 0) {
            return dateCompare;
          }
          // 2차: id 기준 내림차순
          return Long.compare(s2.settlementId(), s1.settlementId());
        })
        .filter(s -> {
          if (finalLastSettlementDate == null) {
            return true;
          }
          int compareDate = s.settlementDate().compareTo(finalLastSettlementDate);
          // 더 이전 날짜
          if (compareDate < 0) {
            return true;
          }
          if (compareDate == 0 && s.settlementId() < finalLastSettlementId) {
            return true; // 같은 날짜, 더 작은 ID
          }
          return false;
        })
        .limit(size + 1) // hasNext 판단용 1개 추가
        .toList();

    // hasNext 판단
    boolean hasNext = filteredSettlement.size() > size;
    List<SettlementResponse> pageSettlements = hasNext ?
        filteredSettlement.subList(0, size) : filteredSettlement;

    // DTO 변환
    List<DeliveredSettlementDetailsDto> deliveredDetailsList = pageSettlements.stream()
        .map(settlement -> new DeliveredSettlementDetailsDto(settlement.settledAmount()))
        .toList();

    // 다음 페이지 토큰 생성
    if (hasNext && !pageSettlements.isEmpty()) {
      SettlementResponse last = pageSettlements.get(pageSettlements.size() - 1);
      nextPageToken = CursorUtil.encode(last.settlementDate().atStartOfDay(), last.settlementId());
    }

    return new CursorPageResponse<>(
        deliveredDetailsList,
        nextPageToken,
        hasNext
    );
  }

  // === 편의 메서드 ===

  // 전체 배달 건 수
  public Integer getTotalDeliveredCount(Long riderProfileId) {
    return settlementBatchService.getSettlements(riderProfileId, ProfileType.RIDER)
        .size();
  }

  // 오늘 배달 건 수
  public Long getTodayCompletedCountByRider(Long riderProfileId) {
    return deliveryRepository.countTodayCompletedDeliveriesByRider(riderProfileId);
  }

  // 이번 주 배달 건 수
  public Integer getThisWeekCompletedCount(Long riderProfileId) {
    return settlementBatchService.getRiderWeekSettlementBatches(riderProfileId)
        .size();
  }

  // 오늘 정산 금액 합계 -> 정산 대기 기준
  public Long getTodayEarningAmountByRiderId(Long riderProfileId) {
    return settlementDetailService.getRiderUnsettledDetail(riderProfileId)
        .scheduledSettleAmount();
  }

  // 이번 주 정산 금액 합계
  public Long getThisWeekEarningAmountByRiderId(Long riderProfileId) {
    return settlementBatchService.getRiderWeekSettlementBatches(riderProfileId)
        .stream()
        .map(SettlementResponse::settledAmount)
        .reduce(0L, Long::sum);
  }

  // 이번 달 정산 금액 합계
  public Long getThisMonthEarningAmountByRiderId(Long riderProfileId) {
    return settlementBatchService.getRiderMonthSettlementBatches(riderProfileId)
        .stream()
        .map(SettlementResponse::settledAmount)
        .reduce(0L, Long::sum);
  }

  // 모든 기간의 정산 완료 금액
  public Long getTotalEarnings(Long riderProfileId) {
    return settlementBatchService.getSettlements(riderProfileId, ProfileType.RIDER)
        .stream()
        .map(SettlementResponse::settledAmount)
        .reduce(0L, Long::sum);
  }

  // 특정 배달 건의 정산 상태 조회
  public String getCompletedDeliverySettlementStatus(Long orderId) {
    return settlementDetailService.getRiderSettlementDetail(orderId)
        .settlementStatus().name();
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