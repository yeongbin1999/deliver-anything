package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "profileImageUrl", column = @Column(name = "customer_profile_image_url", columnDefinition = "TEXT"))
public class CustomerProfile extends BaseProfile {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 기타
  @Column(name = "default_address_id")
  private Long defaultAddressId;

  @Builder
  public CustomerProfile(User user, String nickname, String profileImageUrl) {
    super(nickname, profileImageUrl);
    this.user = user;
  }

  public void updateProfile(String nickname, String profileImageUrl) {
    super.updateNickname(nickname);
    super.updateProfileImageUrl(profileImageUrl);
  }

  public void updateDefaultAddressId(Long addressId) {
    this.defaultAddressId = addressId;
  }

}