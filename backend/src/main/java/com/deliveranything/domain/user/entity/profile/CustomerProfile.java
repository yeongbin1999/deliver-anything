package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.user.entity.User;
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
public class CustomerProfile extends BaseProfile {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(unique = true, nullable = false, length = 20)
  private String nickname;

  @Column(name = "customer_profile_image_url", columnDefinition = "TEXT")
  private String customerProfileImageUrl;

  @Builder
  public CustomerProfile(User user, String nickname, String customerProfileImageUrl) {
    super(nickname);
    this.user = user;
    this.customerProfileImageUrl = customerProfileImageUrl;
  }

  public void setDefaultAddress(Long addressId) {
    user.setDefaultAddress(addressId);
  }

  public void updateProfile(String nickname, String customerProfileImageUrl) {
    super.updateNickname(nickname);
    this.customerProfileImageUrl = customerProfileImageUrl;
  }
}