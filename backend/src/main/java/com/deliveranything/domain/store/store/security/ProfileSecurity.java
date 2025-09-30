package com.deliveranything.domain.store.store.security;

import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.SecurityUser;
import org.springframework.stereotype.Component;

@Component
public class ProfileSecurity {

  public boolean isSeller(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isSellerActive()) {
      throw new CustomException(ErrorCode.ROLE_NOT_ALLOWED);
    }
    return true;
  }

  public boolean isCustomer(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isCustomerActive()) {
      throw new CustomException(ErrorCode.ROLE_NOT_ALLOWED);
    }
    return true;
  }

  public boolean isRider(SecurityUser securityUser) {
    if (securityUser == null || !securityUser.isRiderActive()) {
      throw new CustomException(ErrorCode.ROLE_NOT_ALLOWED);
    }
    return true;
  }
}

