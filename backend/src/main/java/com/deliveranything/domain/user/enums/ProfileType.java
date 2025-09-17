package com.deliveranything.domain.user.enums;

/**
 * 프로필 타입 enum
 */
public enum ProfileType {
  CONSUMER("소비자"),
  SELLER("판매자"),
  RIDER("배달원");

  private final String description;

  ProfileType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}