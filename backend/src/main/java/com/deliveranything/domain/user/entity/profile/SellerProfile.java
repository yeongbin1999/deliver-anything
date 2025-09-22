package com.deliveranything.domain.user.entity.profile;

import com.deliveranything.domain.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
@Table(name = "seller_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
    @AttributeOverride(name = "nickname", column = @Column(name = "seller_nickname", length = 50)),
    @AttributeOverride(name = "profileImageUrl", column = @Column(name = "seller_profile_image_url", columnDefinition = "TEXT"))
})
public class SellerProfile extends BaseProfile {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 사업자 정보
  @Column(name = "business_name", nullable = false, length = 100)
  private String businessName;

  @Column(name = "business_certificate_number", nullable = false, unique = true, length = 20)
  private String businessCertificateNumber;

  @Column(name = "business_phone_number", nullable = false, length = 20)
  private String businessPhoneNumber;

  // 정산 정보
  @Column(name = "seller_bank_name", nullable = false, length = 20)
  private String bankName;

  @Column(name = "seller_bank_account_number", nullable = false, length = 30)
  private String accountNumber;

  @Column(name = "seller_bank_account_holder_name", nullable = false, length = 20)
  private String accountHolder;

  @Builder
  public SellerProfile(User user, String nickname, String profileImageUrl,
      String businessName, String businessCertificateNumber, String businessPhoneNumber,
      String bankName, String accountNumber, String accountHolder) {
    super(nickname, profileImageUrl);
    this.user = user;
    this.businessName = businessName;
    this.businessCertificateNumber = businessCertificateNumber;
    this.businessPhoneNumber = businessPhoneNumber;
    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }
  
}