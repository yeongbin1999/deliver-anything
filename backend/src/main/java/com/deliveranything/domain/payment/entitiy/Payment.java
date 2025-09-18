package com.deliveranything.domain.payment.entitiy;

import com.deliveranything.domain.payment.enums.PaymentMethod;
import com.deliveranything.domain.payment.enums.PaymentStatus;
import com.deliveranything.global.entity.BaseEntity;
import com.deliveranything.refund.entity.Refund;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(nullable = false, unique = true)
  private UUID pgUid;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod paymentMethod;

  @OneToOne(mappedBy = "payment", cascade = CascadeType.PERSIST)
  private Refund refund;
}
