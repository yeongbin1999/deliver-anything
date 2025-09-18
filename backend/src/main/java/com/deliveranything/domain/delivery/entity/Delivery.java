package com.deliveranything.domain.delivery.entity;


import com.deliveranything.domain.delivery.enums.DeliveryStatus;
import com.deliveranything.domain.reviews.entity.Review;
import com.deliveranything.domain.store.entity.Store;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "deliveries")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

  @Column(name = "delivery_remaining_time", nullable = false)
  private Integer remainingTime;

  @Column(name = "delivery_expected_time", nullable = false)
  private Integer expectedTime;

  @Column(name = "delivery_requested")
  private String requested;

  @Column(name = "delivery_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;

  @LastModifiedDate
  @Column(name = "delivery_ended_at")
  private LocalDateTime endedAt;

  @Column(name = "delivery_charge")
  private Integer charge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id")
  private Store store;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "review_id")
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rider_profile_id")
  private RiderProfile rider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_profile_id")
  private CustomerProfile customer;


  @Builder
  public Delivery(Integer remainingTime, Integer expectedTime, String requested,
      DeliveryStatus status, Integer charge) {
    this.remainingTime = remainingTime;
    this.expectedTime = expectedTime;
    this.requested = requested;
    this.status = status;
    this.charge = charge;
  }
}
