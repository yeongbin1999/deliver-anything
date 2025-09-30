package com.deliveranything.domain.delivery.repository;

import com.deliveranything.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

}
