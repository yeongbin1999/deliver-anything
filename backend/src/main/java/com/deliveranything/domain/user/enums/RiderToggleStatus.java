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
}