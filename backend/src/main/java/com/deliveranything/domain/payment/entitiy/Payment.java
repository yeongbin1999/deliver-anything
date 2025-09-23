package com.deliveranything.domain.payment.entitiy;

import com.deliveranything.domain.payment.enums.PaymentStatus;
import com.deliveranything.global.entity.BaseEntity;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(unique = true, length = 200)
  private String paymentKey;

  public Payment(Long orderId, BigDecimal amount) {
    this.orderId = orderId;
    this.amount = amount;
    this.status = PaymentStatus.READY;
  }

  public void updateStatus(PaymentStatus status) {
    if (this.status != PaymentStatus.READY) {
      throw new CustomException(ErrorCode.PAYMENT_INVALID_STATUS);
    }

    this.status = status;
  }
}
