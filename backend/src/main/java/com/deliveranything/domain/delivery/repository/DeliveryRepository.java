package com.deliveranything.domain.delivery.repository;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

  @Query("SELECT COUNT(d) FROM Delivery d WHERE d.status = 'COMPLETED' " +
      "AND d.riderProfile.id = :riderProfileId " +
      "AND DATE(d.completedAt) = CURRENT_DATE")
  Long countTodayCompletedDeliveriesByRider(Long riderProfileId);

  @Query("SELECT COUNT(d) FROM Delivery d WHERE d.riderProfile.id = :riderProfileId " +
      "AND d.status = 'COMPLETED' AND d.completedAt >= :weekStart")
  Long countThisWeekCompletedDeliveriesByRiderProfileId(
      @Param("riderProfileId") Long riderProfileId,
      @Param("weekStart") LocalDateTime weekStart
  );

  @Query("SELECT d FROM Delivery d WHERE d.riderProfile.id = :riderProfileId " +
      "AND d.status = 'COMPLETED' AND DATE(d.completedAt) = CURRENT_DATE")
  List<Delivery> findTodayCompletedDeliveriesByRider(@Param("riderProfileId") Long riderProfileId);

  Optional<Delivery> findByRiderProfileIdAndStatus(Long riderProfileId,
      DeliveryStatus deliveryStatus);

  @Query("SELECT SUM(d.charge) FROM Delivery d WHERE d.riderProfile.id = :riderProfileId " +
      "AND d.status = 'COMPLETED'")
  Long sumTotalDeliveryChargesByRider(Long riderProfileId);

  List<Delivery> findByRiderProfileId(Long riderProfileId);
}
