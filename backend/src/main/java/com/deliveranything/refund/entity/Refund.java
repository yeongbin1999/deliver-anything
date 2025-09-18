package com.deliveranything.refund.entity;

import com.deliveranything.domain.payment.entitiy.Payment;
import com.deliveranything.global.entity.BaseEntity;
import com.deliveranything.refund.enums.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "refunds")
public class Refund extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false, unique = true)
  private Payment payment;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RefundStatus status;

  @Column(nullable = false, unique = true)
  private UUID pgRefundUid;
}