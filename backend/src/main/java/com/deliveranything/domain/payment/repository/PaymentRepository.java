package com.deliveranything.domain.payment.repository;

import com.deliveranything.domain.payment.entitiy.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByMerchantUid(String merchantUid);
}
