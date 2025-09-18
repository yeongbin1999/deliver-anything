package com.deliveranything.domain.settlement.entity;

import com.deliveranything.domain.settlement.enums.TargetType;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
  private BigDecimal targetTotalAmount;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPlatformFee;

  @Column(nullable = false)
  private LocalDate settlementDate;

  @OneToMany(mappedBy = "settlementBatch", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<SettlementDetail> settlementDetails = new ArrayList<>();
}
