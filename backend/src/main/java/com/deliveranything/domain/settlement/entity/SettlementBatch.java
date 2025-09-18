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
import lombok.NoArgsConstructor;

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
  private BigDecimal totalAmount;

  @Column(nullable = false)
  private LocalDate settlementDate;
}
