package com.deliveranything.standard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * 유틸리티 클래스 - static 메서드들을 통해 공통 기능으로 제공
 */
public class Ut {

  /**
   * JWT 관련 유틸리티
   */
  public static class jwt {

    public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
      ClaimsBuilder claimsBuilder = Jwts.claims();

      for (Map.Entry<String, Object> entry : body.entrySet()) {
        claimsBuilder.add(entry.getKey(), entry.getValue());
      }

      Claims claims = claimsBuilder.build();

      Date issuedAt = new Date();
      Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

      Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

      return Jwts.builder()
          .claims(claims)
          .issuedAt(issuedAt)
          .expiration(expiration)
          .signWith(secretKey)
          .compact();
    }

    public static boolean isValid(String secret, String jwtStr) {
      SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

      try {
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwtStr);
      } catch (Exception e) {
        return false;
      }

      return true;
    }

    public static Map<String, Object> payload(String secret, String jwtStr) {
      SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

      try {
        return (Map<String, Object>) Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwtStr)
            .getPayload();
      } catch (Exception e) {
        return null;
      }
    }
  }

  /**
   * JSON 관련 유틸리티
   */
  public static class json {

    public static ObjectMapper objectMapper;

    public static String toString(Object object) {
      return toString(object, null);
    }

    public static String toString(Object object, String defaultValue) {
      try {
        return objectMapper.writeValueAsString(object);
      } catch (Exception e) {
        return defaultValue;
      }
    }

    public static <T> T fromString(String jsonString, Class<T> clazz) {
      return fromString(jsonString, clazz, null);
    }

    public static <T> T fromString(String jsonString, Class<T> clazz, T defaultValue) {
      try {
        return objectMapper.readValue(jsonString, clazz);
      } catch (Exception e) {
        return defaultValue;
      }
    }
  }
}
