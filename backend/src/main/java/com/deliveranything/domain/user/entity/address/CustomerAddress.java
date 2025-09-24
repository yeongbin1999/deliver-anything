package com.deliveranything.domain.user.entity.address;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.global.entity.BaseEntity;
import com.deliveranything.global.util.PointUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "customer_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerAddress extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_profile_id", nullable = false)
  private CustomerProfile customerProfile;

  @Column(name = "address_name", length = 50)
  private String addressName;

  @Column(nullable = false, length = 100)
  private String address;

  // 위도 경도를 변경 Point 객체로 변경
  @Column(columnDefinition = "POINT SRID 4326")
  private Point location;

  @Builder
  public CustomerAddress(CustomerProfile customerProfile, String addressName,
      String address, Double latitude,
      Double longitude) {
    this.customerProfile = customerProfile;
    this.addressName = addressName;
    this.address = address;
    // 위도/경도를 받아서 Point 객체로 변환
    this.location = PointUtil.createPoint(latitude, longitude);
  }

  // 비즈니스 메서드
  public boolean isDefault() {
    return customerProfile.getDefaultAddressId() != null &&
        customerProfile.getDefaultAddressId().equals(this.getId());
  }

  public void updateAddress(String addressName, String address,
      Double latitude, Double longitude) {
    this.addressName = addressName;
    this.address = address;
    // 위도/경도를 Point 객체로 변환
    this.location = PointUtil.createPoint(latitude, longitude);

  }

  // 편의 메서드: 위도/경도 개별 접근
  public Double getLatitude() {
    return location != null ? location.getY() : null;
  }

  public Double getLongitude() {
    return location != null ? location.getX() : null;
  }
}