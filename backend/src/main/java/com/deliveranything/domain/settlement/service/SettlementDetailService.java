package com.deliveranything.domain.settlement.service;

import com.deliveranything.domain.settlement.entity.SettlementDetail;
import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.domain.settlement.repository.SettlementDetailRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
}
