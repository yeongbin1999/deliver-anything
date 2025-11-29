package com.deliveranything.global.util;

import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public final class CursorUtil {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(
      new JavaTimeModule());

  private CursorUtil() {
  }

  public static String encode(Object cursorObj) {
    if (cursorObj == null) {
      log.warn("null 커서 객체를 인코딩 시도했습니다. null을 반환합니다.");
      return null;
    }

    try {
      byte[] jsonBytes = OBJECT_MAPPER.writeValueAsBytes(cursorObj);
      return Base64.getUrlEncoder().encodeToString(jsonBytes);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.CURSOR_ENCODE_FAILED);
    }
  }

  public static <T> T decode(String cursor, Class<T> clazz) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }

    try {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
      return OBJECT_MAPPER.readValue(decodedBytes, clazz);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.CURSOR_DECODE_FAILED);
    }
  }

  // 위에 더 유연한 구조 사용하면 좋을 것 같아요
  // encode 할때 도메인의 커서 객체로, decode할때 도메인의 커서 객체로

  @Deprecated
  public static String encode(Object... keys) {
    if (keys == null || keys.length == 0) {
      log.warn("null 또는 빈 키 배열을 인코딩 시도했습니다. null을 반환합니다.");
      return null;
    }
    try {
      byte[] jsonBytes = OBJECT_MAPPER.writeValueAsBytes(keys);
      return Base64.getUrlEncoder().encodeToString(jsonBytes);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.CURSOR_ENCODE_FAILED);
    }
  }

  @Deprecated
  public static Object[] decode(String cursor) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }
    try {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
      return OBJECT_MAPPER.readValue(decodedBytes, Object[].class);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.CURSOR_DECODE_FAILED);
    }
  }
}