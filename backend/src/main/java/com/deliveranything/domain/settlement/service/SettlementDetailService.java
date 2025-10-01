package com.deliveranything.domain.settlement.service;

import com.deliveranything.domain.settlement.dto.SettlementDetailResponse;
import com.deliveranything.domain.settlement.dto.UnsettledResponse;
import com.deliveranything.domain.settlement.entity.SettlementDetail;
import com.deliveranything.domain.settlement.enums.SettlementStatus;
import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.domain.settlement.repository.SettlementDetailRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SettlementDetailService {

  private final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.08");
  private final SettlementDetailRepository settlementDetailRepository;

  @Transactional
  public void createSellerSettlement(Long orderId, Long sellerProfileId, BigDecimal storePrice) {
    BigDecimal platformFee = storePrice
        .multiply(PLATFORM_FEE_RATE)
        .setScale(2, RoundingMode.HALF_UP);

    SettlementDetail settlementDetail = SettlementDetail.builder()
        .orderId(orderId)
        .targetType(TargetType.SELLER)
        .targetId(sellerProfileId)
        .targetAmount(storePrice.subtract(platformFee))
        .platformFee(platformFee)
        .build();

    settlementDetailRepository.save(settlementDetail);
  }

  @Transactional
  public void createRiderSettlement(Long orderId, Long riderProfileId, BigDecimal deliveryPrice) {
    SettlementDetail settlementDetail = SettlementDetail.builder()
        .orderId(orderId)
        .targetType(TargetType.SELLER)
        .targetId(riderProfileId)
        .targetAmount(deliveryPrice)
        .platformFee(BigDecimal.valueOf(0))
        .build();

    settlementDetailRepository.save(settlementDetail);
  }

  // 배달원이 배달한 주문의 정산 정보 조회
  @Transactional(readOnly = true)
  public SettlementDetailResponse getRiderSettlementDetail(Long orderId) {
    return SettlementDetailResponse.from(
        settlementDetailRepository.findByOrderIdAndTargetType(orderId, TargetType.RIDER)
            .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_DETAIL_NOT_FOUND)));
  }

  // 배달원의 금일 정산 대기 정보
  public UnsettledResponse getRiderUnsettledDetail(Long riderProfileId) {
    LocalDate today = LocalDate.now();
    return UnsettledResponse.from(
        settlementDetailRepository.findAllUnsettledDetails(TargetType.RIDER, riderProfileId,
            SettlementStatus.PENDING, today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
  }
}
