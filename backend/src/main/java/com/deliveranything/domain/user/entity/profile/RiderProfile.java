package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "rider_priofile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiderProfile extends BaseEntity {

  @Column(name = "rider_nickname", nullable = false)
  private String nickname;

  @Column(name = "rider_toggle_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private RiderToggleStatus toggleStatus;

  @Column(name = "rider_area")
  private String area;     // 추후 Enum으로 변경 고려

  @Column(name = "rider_license_number", nullable = false, unique = true)
  private String licenseNumber;

  @Column(name = "rider_profile_image_url")
  private String profileImageUrl;

  @Column(name = "rider_bank_name")
  private String bankName;

  @Column(name = "rider_bank_account_number")
  private String bankAccountNumber;

  @Column(name = "rider_bank_account_holder_name")
  private String bankAccountHolderName;

  @OneToOne(mappedBy = "riderProfile", fetch = FetchType.LAZY)
  private User user;

  @OneToMany(mappedBy = "riderProfile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Delivery> deliveries = new ArrayList<>();

  @Builder
  public RiderProfile(String nickname, RiderToggleStatus toggleStatus, String area,
      String licenseNumber, String profileImageUrl, String bankName,
      String bankAccountNumber, String bankAccountHolderName, User user) {
    this.nickname = nickname;
    this.toggleStatus = toggleStatus;
    this.area = area;
    this.licenseNumber = licenseNumber;
    this.profileImageUrl = profileImageUrl;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountHolderName = bankAccountHolderName;
    this.user = user;
  }

}
