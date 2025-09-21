package com.deliveranything.domain.store.store.entity;

import com.deliveranything.domain.store.store.enums.StoreCategoryType;
import com.deliveranything.domain.store.store.enums.StoreStatus;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.global.util.PointUtil;
import lombok.AccessLevel;
import lombok.Builder;
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
        @Index(name = "idx_stores_location", columnList = "location")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

  @Column(name = "seller_profile_id", nullable = false)
  private Long sellerProfileId;

  @Enumerated(EnumType.STRING)
  @Column(name = "store_category", nullable = false)
  private StoreCategoryType storeCategory;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "road_addr", length = 255)
  private String roadAddr;

  @Column(columnDefinition = "POINT SRID 4326")
  private Point location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 12)
  private StoreStatus status = StoreStatus.DRAFT;

  @Column(name = "is_open_now", nullable = false)
  private boolean isOpenNow = false;

  @Column(name = "open_hours_json", columnDefinition = "TEXT") // H2/MySQL 호환을 위해 TEXT로 매핑
  private String openHoursJson;

  @Column(name = "next_change_at")
  private LocalDateTime nextChangeAt;

  @Builder
  public Store(Long sellerProfileId, StoreCategoryType storeCategory, String name, String roadAddr, Point location, String openHoursJson) {
    this.sellerProfileId = sellerProfileId;
    this.storeCategory = storeCategory;
    this.name = name;
    this.roadAddr = roadAddr;
    this.location = location;
    this.openHoursJson = openHoursJson;
  }

  public void update(StoreUpdateRequest request) {
    if (request.storeCategory() != null) {
      this.storeCategory = request.storeCategory();
    }
    if (request.name() != null) {
      this.name = request.name();
    }
    if (request.roadAddr() != null) {
      this.roadAddr = request.roadAddr();
    }
    if (request.lat() != null && request.lng() != null) {
      this.location = PointUtil.createPoint(request.lat(), request.lng());
    }
    if (request.openHoursJson() != null) {
      this.openHoursJson = request.openHoursJson();
    }
  }
}