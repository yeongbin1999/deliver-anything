package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.user.enums.RiderToggleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rider_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// BaseProfile 상속 반영
public class RiderProfile extends BaseProfile {

  @Id
  private Long id; // Profile.id와 동일한 값

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Profile profile;

  // BaseProfile에서 상속받은 필드: nickname, profileImageUrl

  @Enumerated(EnumType.STRING)
  @Column(name = "toggle_status", nullable = false, columnDefinition = "VARCHAR(10)")
  private RiderToggleStatus toggleStatus;

  @Column(name = "area", nullable = false, columnDefinition = "VARCHAR(100)")
  private String area;

  @Column(name = "license_number", nullable = false, columnDefinition = "VARCHAR(50)")
  private String licenseNumber;

  @Column(name = "bank_name", columnDefinition = "VARCHAR(50)")
  private String bankName;

  @Column(name = "bank_account_number", columnDefinition = "VARCHAR(50)")
  private String bankAccountNumber;

  @Column(name = "bank_account_holder_name", columnDefinition = "VARCHAR(50)")
  private String bankAccountHolderName;

  @Builder
  public RiderProfile(Profile profile, String nickname, String profileImageUrl,
      RiderToggleStatus toggleStatus, String area, String licenseNumber,
      String bankName, String bankAccountNumber, String bankAccountHolderName) {

    // 부모 클래스(BaseProfile)의 생성자 호출 (공통 필드 초기화)
    super(nickname, profileImageUrl);

    this.profile = profile;
    this.toggleStatus = toggleStatus != null ? toggleStatus : RiderToggleStatus.OFF;
    this.area = area;
    this.licenseNumber = licenseNumber;
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountHolderName = bankAccountHolderName;
  }

  // 비즈니스 메서드
  // 부모 클래스(BaseProfile)의 updateNickname/updateProfileImageUrl 메서드를 사용하거나,
  // 필요에 따라 오버라이드 가능합니다. 현재는 부모 메서드를 사용하도록 정리합니다.
  public void updateProfile(String nickname, String profileImageUrl) {
    if (nickname != null && !nickname.isBlank()) {
      super.updateNickname(nickname); // BaseProfile의 메서드 호출
    }
    super.updateProfileImageUrl(profileImageUrl); // BaseProfile의 메서드 호출
  }

  public void updateBankInfo(String bankName, String bankAccountNumber,
      String bankAccountHolderName) {
    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.bankAccountHolderName = bankAccountHolderName;
  }

  public void toggleStatus() {
    this.toggleStatus = this.toggleStatus == RiderToggleStatus.ON
        ? RiderToggleStatus.OFF
        : RiderToggleStatus.ON;
  }

  public void setToggleStatus(RiderToggleStatus status) {
    this.toggleStatus = status;
  }

  // User 정보 접근용 헬퍼 메서드
  public Long getUserId() {
    return profile != null ? profile.getUser().getId() : null;
  }
}