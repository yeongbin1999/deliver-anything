package com.deliveranything.domain.payment.service;

import com.deliveranything.domain.order.service.OrderService;
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

  private final OrderService orderService;

  private final PaymentRepository paymentRepository;

  @Transactional
  public void createPayment(Long orderId, BigDecimal amount) {
    paymentRepository.save(new Payment(orderId, amount));
  }

  @Transactional
  public void cancelPayment(Long paymentId) {
    getPayment(paymentId).updateStatus(PaymentStatus.CANCELED);
  }

  @Transactional
  public void confirmPayment(Long paymentId, PaymentConfirmRequest request) {
    Payment payment = getPayment(paymentId);
    long orderPrice = orderService.getOrderByMerchantId(request.merchantUid()).totalPrice()
        .longValue();

    if (orderPrice != request.amount()) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PAYMENT_AMOUNT_NOT_VALID);
    }

    WebClient webClient = webClientBuilder.baseUrl(tossPaymentsConfig.getTossUrl()).build();
    String encodedSecretKey = Base64.getEncoder()
        .encodeToString((tossPaymentsConfig.getSecretKey() + ":").getBytes());

    PaymentConfirmResponse pgResponse = webClient.post()
        .uri("/v1/payments/confirm")
        .headers(headers -> {
          headers.setBasicAuth(encodedSecretKey);
          headers.setContentType(MediaType.APPLICATION_JSON);
        })
        .bodyValue(new PaymentConfirmRequest(request.paymentKey(), request.merchantUid(),
            request.amount()))
        .retrieve()
        .bodyToMono(PaymentConfirmResponse.class)
        .block();

    if (pgResponse == null) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PG_PAYMENT_NOT_FOUND);
    }

    if (!(orderPrice == pgResponse.totalAmount() && request.paymentKey()
        .equals(pgResponse.paymentKey()) && request.merchantUid().equals(pgResponse.orderId()))) {
      payment.updateStatus(PaymentStatus.FAILED);
      throw new CustomException(ErrorCode.PG_PAYMENT_CONFIRM_FAILED);
    }

    payment.updateStatus(PaymentStatus.CANCELED);
  }

  private Payment getPayment(Long paymentId) {
    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
  }
}
