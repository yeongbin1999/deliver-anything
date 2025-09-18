package com.deliveranything.domain.settlement.entity;

import com.deliveranything.domain.settlement.enums.SettlementStatus;
import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "settlement_details")
public class SettlementDetail extends BaseEntity {

  @Column(nullable = false)
  private Long orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TargetType targetType;

  @Column(nullable = false)
  private Long targetId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SettlementStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_batch_id")
  private SettlementBatch settlementBatch;
}
