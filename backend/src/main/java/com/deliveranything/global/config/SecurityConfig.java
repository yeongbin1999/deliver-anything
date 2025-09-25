package com.deliveranything.global.config;

import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.security.CustomAuthenticationFilter;
import com.deliveranything.standard.util.Ut;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationFilter customAuthenticationFilter;
  
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스 허용
            .requestMatchers("/favicon.ico", "/error").permitAll()
            .requestMatchers("/h2-console/**").permitAll()

            // Swagger 관련 허용
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // 공개 API 엔드포인트
            .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/logout")
            .permitAll()
            .requestMatchers("/api/v1/auth/verification/**").permitAll()
            .requestMatchers("/api/v1/auth/oauth2/**").permitAll()

            // 관리자 전용 API
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // 프로필별 API 권한 (Method Security로 세밀한 제어)
            .requestMatchers("/api/v1/users/me/customer/**").hasRole("CUSTOMER")
            .requestMatchers("/api/v1/users/me/seller/**").hasRole("SELLER")
            .requestMatchers("/api/v1/users/me/rider/**").hasRole("RIDER")

            // 판매자 관련 API
            .requestMatchers("/api/v1/stores/**").hasAnyRole("SELLER", "CUSTOMER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/stores/**").hasRole("SELLER")
            .requestMatchers(HttpMethod.PUT, "/api/v1/stores/**").hasRole("SELLER")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/stores/**").hasRole("SELLER")

            // 주문 관련 API
            .requestMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "SELLER", "RIDER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("CUSTOMER")

            // 배달 관련 API
            .requestMatchers("/api/v1/deliveries/**")
            .hasAnyRole("RIDER", "CUSTOMER", "SELLER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/deliveries/*/status").hasRole("RIDER")

            // 인증이 필요한 나머지 API
            .requestMatchers("/api/v1/**").authenticated()

            // 그 외 모든 요청 허용
            .anyRequest().permitAll()
        )
        .headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        )

        // ✅ CORS 설정 적용 추가
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(AbstractHttpConfigurer::disable)

        // 커스텀 인증 필터 등록
        .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // 예외 처리
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((request, response, authException) -> {
              response.setContentType("application/json;charset=UTF-8");
              response.setStatus(401);

              ApiResponse<Void> apiResponse = ApiResponse.fail(
                  "AUTH-401",
                  "로그인 후 이용해주세요."
              );
              response.getWriter().write(Ut.json.toString(apiResponse));
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setContentType("application/json;charset=UTF-8");
              response.setStatus(403);

              ApiResponse<Void> apiResponse = ApiResponse.fail(
                  "AUTH-403",
                  "해당 기능을 사용할 권한이 없습니다. 프로필을 확인해주세요."
              );
              response.getWriter().write(Ut.json.toString(apiResponse));
            })
        );

    return http.build();
  }

  /**
   * CORS 설정
   */
  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 허용할 오리진 설정
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:3000",    // React 개발 서버
        "http://localhost:8080",    // Spring Boot 서버
        "https://*.deliver-anything.shop",  // 배포 도메인
        "https://cdpn.io"          // CodePen 테스트
    ));

    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    // Authorization 헤더 노출 (JWT 토큰용)
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);

    return source;
  }
}