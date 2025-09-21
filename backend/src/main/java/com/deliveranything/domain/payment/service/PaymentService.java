package com.deliveranything.domain.payment.service;

import com.deliveranything.domain.payment.entitiy.Payment;
import com.deliveranything.domain.payment.repository.PaymentRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public Long createPayment(Long orderId, BigDecimal amount) {
    return paymentRepository.save(new Payment(orderId, amount)).getId();
  }

  @Transactional
  public void cancelPayment(Long paymentId) {
    getPayment(paymentId).cancel();
  }

  private Payment getPayment(Long paymentId) {
    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
  }
}
