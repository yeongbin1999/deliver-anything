package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "rider_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "profileImageUrl", column = @Column(name = "rider_profile_image_url", columnDefinition = "TEXT"))
public class RiderProfile extends BaseProfile {

  @Column(name = "rider_toggle_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private RiderToggleStatus toggleStatus;

  @Column(name = "rider_area")
  private String area;     // 추후 Enum, List로 변경 고려

  @Column(name = "rider_license_number", nullable = false, unique = true)
  private String licenseNumber;

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
    super(nickname, profileImageUrl); // 부모 클래스 생성자 호출
    this.toggleStatus = toggleStatus;
    this.area = area;
    this.licenseNumber = licenseNumber;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountHolderName = bankAccountHolderName;
    this.user = user;
  }

  public void setToggleStatus(RiderToggleStatus toggleStatus) {
    this.toggleStatus = toggleStatus;
  }

  // String을 받아서 enum으로 변환하는 메서드 추가
  public void setToggleStatus(String toggleStatus) {
    this.toggleStatus = RiderToggleStatus.fromString(toggleStatus);
  }

  public void setDeliveryArea(@NotNull String area) {
    this.area = area;
  }

  // 상속받은 요소는 이렇게 업데이트!! 주석처리만 해둘게요
//  public void updateProfile(String nickname, String profileImageUrl) {
//    super.updateNickname(nickname);
//    super.updateProfileImageUrl(profileImageUrl);
//  }

}
