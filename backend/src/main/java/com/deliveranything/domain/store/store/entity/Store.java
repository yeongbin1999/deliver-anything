package com.deliveranything.domain.store.store.entity;

import com.deliveranything.domain.store.categoty.entity.StoreCategory;
import com.deliveranything.domain.store.store.enums.StoreStatus;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.locationtech.jts.geom.Point;

@Entity
@Table(
    name = "stores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_stores_seller", columnNames = "seller_profile_id")
    },
    indexes = {
        @Index(name = "idx_stores_category", columnList = "store_category_id"),
        @Index(name = "idx_stores_status", columnList = "status"),
        @Index(name = "idx_stores_location", columnList = "location") // 공간 인덱스를 위한 일반 인덱스 추가
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

  @Column(name = "seller_profile_id", nullable = false)
  private Long sellerProfileId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_category_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_stores_store_category"))
  private StoreCategory storeCategory;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "road_addr", length = 255)
  private String roadAddr;

  @Column(columnDefinition = "POINT SRID 4326")
  private Point location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 12)
  private StoreStatus status = StoreStatus.DRAFT;

  @Column(name = "open_hours_json", columnDefinition = "TEXT") // H2/MySQL 호환을 위해 TEXT로 매핑
  private String openHoursJson;

  @Column(name = "is_open_now", nullable = false)
  private boolean isOpenNow = false;

  @Column(name = "accepting_orders", nullable = false)
  private boolean acceptingOrders = true;

  @Column(name = "next_change_at")
  private LocalDateTime nextChangeAt;

  public Store(Long sellerProfileId, StoreCategory storeCategory, String name) {
    this.sellerProfileId = sellerProfileId;
    this.storeCategory = storeCategory;
    this.name = name;
  }
}