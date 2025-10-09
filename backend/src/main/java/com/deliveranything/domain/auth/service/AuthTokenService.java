package com.deliveranything.domain.auth.service;

import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthTokenService {

  @Value("${custom.jwt.secretKey}")
  private String jwtSecretKey;

  @Value("${custom.accessToken.expirationSeconds}")
  private int accessTokenExpirationSeconds;

  /**
   * 사용자 정보를 기반으로 JWT Access Token 생성 (전역 Profile ID 사용)
   */
  public String genAccessToken(User user) {
    long id = user.getId();
    String username = user.getUsername();

    // 멀티 프로필 정보 (이제 전역 고유 Profile ID)
    ProfileType currentActiveProfileType = user.getCurrentActiveProfileType();
    Long currentActiveProfileId = user.getCurrentActiveProfileId(); // 전역 고유 ID

    ClaimsBuilder claimsBuilder = Jwts.claims();

    // 필수 정보 + UI용 name
    claimsBuilder.add("id", id);
    claimsBuilder.add("name", username);
    claimsBuilder.add("currentActiveProfile",
        currentActiveProfileType != null ? currentActiveProfileType.name() : null);
    claimsBuilder.add("currentActiveProfileId", currentActiveProfileId);

    Claims claims = claimsBuilder.build();

    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + 1000L * accessTokenExpirationSeconds);

    Key secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

    return Jwts.builder()
        .claims(claims)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  /**
   * JWT Access Token에서 페이로드 파싱 필수 정보 + name 추출 (전역 Profile ID 포함)
   */
  public Map<String, Object> payload(String accessToken) {
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

    try {
      Claims claims = (Claims) Jwts
          .parser()
          .verifyWith(secretKey)
          .build()
          .parse(accessToken)
          .getPayload();

      // 필수 정보 + UI용 name 추출
      Long id = claims.get("id", Long.class);
      String username = claims.get("name", String.class);

      // 멀티 프로필 정보 추출 (전역 고유 Profile ID)
      String currentActiveProfileStr = claims.get("currentActiveProfile", String.class);
      ProfileType currentActiveProfile = currentActiveProfileStr != null ?
          ProfileType.valueOf(currentActiveProfileStr) : null;
      Long currentActiveProfileId = claims.get("currentActiveProfileId", Long.class);

      return Map.of(
          "id", id,
          "name", username != null ? username : "",
          "currentActiveProfile", currentActiveProfile,
          "currentActiveProfileId", currentActiveProfileId != null ? currentActiveProfileId : 0L
      );

    } catch (Exception e) {
      log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  // JWT 토큰 유효성 검증
  public boolean isValidToken(String accessToken) {
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

    try {
      Jwts
          .parser()
          .verifyWith(secretKey)
          .build()
          .parse(accessToken);
      return true;
    } catch (Exception e) {
      log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
      return false;
    }
  }

  // JWT 토큰에서 만료 시간 추출
  public Date getExpirationDate(String accessToken) {
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

    try {
      Claims claims = (Claims) Jwts
          .parser()
          .verifyWith(secretKey)
          .build()
          .parse(accessToken)
          .getPayload();

      return claims.getExpiration();
    } catch (Exception e) {
      log.warn("JWT 만료 시간 추출 실패: {}", e.getMessage());
      return null;
    }
  }

  // 토큰이 만료되었는지 확인
  public boolean isTokenExpired(String accessToken) {
    Date expiration = getExpirationDate(accessToken);
    return expiration != null && expiration.before(new Date());
  }

  // JWT 토큰에서 사용자 ID 추출 (빠른 조회)
  public Long getUserId(String accessToken) {
    Map<String, Object> payload = payload(accessToken);
    return payload != null ? (Long) payload.get("id") : null;
  }

  // JWT 토큰에서 현재 활성 프로필 타입 추출 (권한 체크용)
  public ProfileType getCurrentActiveProfile(String accessToken) {
    Map<String, Object> payload = payload(accessToken);
    return payload != null ? (ProfileType) payload.get("currentActiveProfile") : null;
  }

  // JWT 토큰에서 현재 활성 프로필 ID 추출 (전역 고유 ID)
  public Long getCurrentActiveProfileId(String accessToken) {
    Map<String, Object> payload = payload(accessToken);
    return payload != null ? (Long) payload.get("currentActiveProfileId") : null;
  }
}