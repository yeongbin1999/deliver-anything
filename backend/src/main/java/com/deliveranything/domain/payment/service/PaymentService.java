package com.deliveranything.domain.payment.service;

import com.deliveranything.domain.payment.config.TossPaymentsConfig;
import com.deliveranything.domain.payment.dto.PaymentConfirmRequest;
import com.deliveranything.domain.payment.dto.PaymentConfirmResponse;
import com.deliveranything.domain.payment.entitiy.Payment;
import com.deliveranything.domain.payment.enums.PaymentStatus;
import com.deliveranything.domain.payment.repository.PaymentRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class PaymentService {

  private final TossPaymentsConfig tossPaymentsConfig;
  private final WebClient.Builder webClientBuilder;

  private final PaymentRepository paymentRepository;

  @Transactional
  public void createPayment(String merchantUid, BigDecimal amount) {
    paymentRepository.save(new Payment(merchantUid, amount));
  }

  @Transactional
  public void confirmPayment(String paymentKey, String merchantUid, long orderAmount) {
    Payment payment = getPayment(merchantUid);

    if (orderAmount != payment.getAmount().longValue()) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PAYMENT_AMOUNT_INVALID);
    }

    // 토스페이먼츠 결제 승인 API 호출 준비
    WebClient webClient = webClientBuilder.baseUrl(tossPaymentsConfig.getTossUrl()).build();
    String encodedSecretKey = Base64.getEncoder()
        .encodeToString((tossPaymentsConfig.getSecretKey() + ":").getBytes());

    // 응답 수신 확인
    PaymentConfirmResponse pgResponse = webClient.post()
        .uri("/v1/payments/confirm")
        .headers(headers -> {
          headers.setBasicAuth(encodedSecretKey);
          headers.setContentType(MediaType.APPLICATION_JSON);
        })
        .bodyValue(new PaymentConfirmRequest(paymentKey, merchantUid, orderAmount))
        .retrieve()
        .bodyToMono(PaymentConfirmResponse.class)
        .block();

    if (pgResponse == null) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PG_PAYMENT_NOT_FOUND);
    }

    // zero trust 검증
    if (!(paymentKey.equals(pgResponse.paymentKey()) && merchantUid.equals(pgResponse.orderId())
        && orderAmount == pgResponse.totalAmount())) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PG_PAYMENT_CONFIRM_FAILED);
    }

    payment.updateStatus(PaymentStatus.PAID);
  }

  private Payment getPayment(String merchantUid) {
    return paymentRepository.findByMerchantUid(merchantUid)
        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
  }
}
