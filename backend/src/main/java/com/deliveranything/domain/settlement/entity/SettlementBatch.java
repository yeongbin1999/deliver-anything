package com.deliveranything.domain.settlement.entity;

import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TargetType targetType;

  @Column(nullable = false)
  private Long targetId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal targetTotalAmount;

  @Column(nullable = false)
  private Integer transactionCount;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPlatformFee;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal settledAmount;

  @Column(nullable = false)
  private LocalDate settlementDate;

  @Builder
  public SettlementBatch(TargetType targetType, Long targetId, BigDecimal targetTotalAmount,
      Integer transactionCount, BigDecimal totalPlatformFee, BigDecimal settledAmount,
      LocalDate settlementDate) {
    this.targetType = targetType;
    this.targetId = targetId;
    this.targetTotalAmount = targetTotalAmount;
    this.transactionCount = transactionCount;
    this.totalPlatformFee = totalPlatformFee;
    this.settledAmount = settledAmount;
    this.settlementDate = settlementDate;
  }
}
