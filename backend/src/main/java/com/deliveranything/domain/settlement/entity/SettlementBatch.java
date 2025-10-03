package com.deliveranything.domain.settlement.entity;

import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "settlement_batches")
public class SettlementBatch extends BaseEntity {

  @Column(nullable = false)
  private Long targetId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal targetTotalAmount;

  @Column(nullable = false)
  private Long transactionCount;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPlatformFee;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal settledAmount;

  @Column(nullable = false)
  private LocalDate settlementDate;

  @Builder
  public SettlementBatch(Long targetId, BigDecimal targetTotalAmount, Long transactionCount,
      BigDecimal totalPlatformFee, BigDecimal settledAmount, LocalDate settlementDate) {
    this.targetId = targetId;
    this.targetTotalAmount = targetTotalAmount;
    this.transactionCount = transactionCount;
    this.totalPlatformFee = totalPlatformFee;
    this.settledAmount = settledAmount;
    this.settlementDate = settlementDate;
  }
}
