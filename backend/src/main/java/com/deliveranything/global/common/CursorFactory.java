package com.deliveranything.global.common;

import com.deliveranything.global.util.CursorUtil;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CursorFactory {

  public static <E, D, C> CursorPageResponse<D> create(
      List<E> entities,
      long pageSize,
      Function<E, D> toDto,
      Function<E, C> toCursor
  ) {
    // DTO 리스트로 변환 (응답 본문에 포함될 실제 페이지 크기만큼만)
    List<D> dtos = entities.stream()
        .limit(pageSize)
        .map(toDto)
        .toList();

    boolean hasNext = entities.size() > pageSize;
    String nextPageToken = null;

    if (hasNext) {
      E lastEntity = entities.get((int) pageSize - 1);
      C cursor = toCursor.apply(lastEntity);
      nextPageToken = CursorUtil.encode(cursor);
    }

    return new CursorPageResponse<>(dtos, nextPageToken, hasNext);
  }
}
