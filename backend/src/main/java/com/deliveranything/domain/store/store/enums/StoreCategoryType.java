package com.deliveranything.domain.store.store.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreCategoryType {
  FOOD_CAFE(10, "음식/카페"),
  CONVENIENCE_STORE(20, "편의점/마트"),
  LIFE_GOODS(30, "생활/잡화"),
  BEAUTY_HEALTH(40, "뷰티/헬스"),
  ELECTRONICS_IT(50, "전자/IT"),
  FASHION(60, "패션/의류"),
  SPORTS_LEISURE(70, "스포츠/레저"),
  BOOK_CULTURE(80, "서점/문화"),
  PET_SUPPLIES(90, "반려동물용품"),
  FLOWERS_GARDEN(100, "꽃/원예"),
  CAR_TOOLS(110, "자동차/공구"),
  TOOLS_HARDWARE(120, "공구/철물");

  private final int id;
  private final String displayName;

  public static StoreCategoryType fromId(long categoryId) {
    for (StoreCategoryType type : values()) {
      if (type.getId() == categoryId) {
        return type;
      }
    }
    return null;
  }
}