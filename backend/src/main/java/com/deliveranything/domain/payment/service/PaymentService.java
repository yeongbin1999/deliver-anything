package com.deliveranything.domain.payment.service;

import com.deliveranything.domain.payment.entitiy.Payment;
import com.deliveranything.domain.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public void createPayment(Long orderId, BigDecimal amount) {
    paymentRepository.save(new Payment(orderId, amount));
  }
}
