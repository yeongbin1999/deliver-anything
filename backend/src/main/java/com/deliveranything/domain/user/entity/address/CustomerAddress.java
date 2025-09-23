package com.deliveranything.domain.user.entity.address;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.global.entity.BaseEntity;
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

  @Column(precision = 10, scale = 7) // 10 , 7  값에 대해서는 협의 필요 (위도 경도 범위)
  private Double latitude;

  @Column(precision = 10, scale = 7)
  private Double longitude;

  @Builder
  public CustomerAddress(CustomerProfile customerProfile, String addressName,
      String address, Double latitude,
      Double longitude) {
    this.customerProfile = customerProfile;
    this.addressName = addressName;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
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
    this.latitude = latitude;
    this.longitude = longitude;
  }
}