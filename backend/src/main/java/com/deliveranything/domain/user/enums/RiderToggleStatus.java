package com.deliveranything.domain.user.enums;

public enum RiderToggleStatus {
  ON("활성"),
  OFF("비활성");

  private final String description;

  RiderToggleStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  // String을 RiderToggleStatus로 변환 (enum 이름으로)
  public static RiderToggleStatus fromString(String status) {
    return RiderToggleStatus.valueOf(status.toUpperCase());
    
  }
}