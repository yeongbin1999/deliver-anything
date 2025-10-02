package com.deliveranything.global.security.expression;

import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.auth.SecurityUser;
import org.springframework.stereotype.Component;

@Component
public class ProfileSecurity {

  public boolean isSeller(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isSellerActive()) {
      throw new CustomException(ErrorCode.PROFILE_NOT_ALLOWED);
    }
    return true;
  }

  public boolean isCustomer(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isCustomerActive()) {
      throw new CustomException(ErrorCode.PROFILE_NOT_ALLOWED);
    }
    return true;
  }

  public boolean isRider(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isRiderActive()) {
      throw new CustomException(ErrorCode.PROFILE_NOT_ALLOWED);
    }
    return true;
  }
}

