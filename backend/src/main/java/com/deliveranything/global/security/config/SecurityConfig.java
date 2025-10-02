package com.deliveranything.global.security.config;

import com.deliveranything.global.security.filter.CustomAuthenticationFilter;
import com.deliveranything.global.security.handler.CustomAccessDeniedHandler;
import com.deliveranything.global.security.handler.CustomAuthenticationEntryPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationFilter customAuthenticationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final AuthenticationSuccessHandler customOAuth2LoginSuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // CSRF, FormLogin, Logout, Basic Auth 모두 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        // 세션 완전 비활성화 (JWT 환경)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // OAuth2 로그인 설정 (소셜 로그인)
        .oauth2Login(oauth2Login -> oauth2Login
            .successHandler(customOAuth2LoginSuccessHandler)
        )

        // CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 권한 설정
        .authorizeHttpRequests(auth -> auth
                // 정적 리소스 및 H2 콘솔
                .requestMatchers("/favicon.ico", "/error", "/h2-console/**").permitAll()

                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

//            현재 필요없음
//            // 관리자
//            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//
//            // 사용자별 권한
//            .requestMatchers("/api/v1/users/me/customer/**").hasRole("CUSTOMER")
//            .requestMatchers("/api/v1/users/me/seller/**").hasRole("SELLER")
//            .requestMatchers("/api/v1/users/me/rider/**").hasRole("RIDER")
//
//            // 판매자 관련
//            .requestMatchers(HttpMethod.POST, "/api/v1/stores/**").hasRole("SELLER")
//            .requestMatchers(HttpMethod.PUT, "/api/v1/stores/**").hasRole("SELLER")
//            .requestMatchers(HttpMethod.DELETE, "/api/v1/stores/**").hasRole("SELLER")
//
//            // 주문 관련
//            .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("CUSTOMER")
//
//            // 배달 관련
//            .requestMatchers(HttpMethod.PUT, "/api/v1/deliveries/*/status").hasRole("RIDER")
//
//            // 나머지 API는 인증 필요
//            .requestMatchers("/api/v1/**").authenticated()

                // 그 외 요청 허용
                .anyRequest().permitAll()
        )

        // H2 콘솔 frame 옵션 허용
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

        // 예외 처리
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler)
        )

        // 커스텀 인증 필터
        .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 허용할 오리진
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:*",
        "https://www.deliver-anything.shop",
        "https://api.deliver-anything.shop",
        "https://cdpn.io"
    ));

    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    // Authorization 헤더 노출 (JWT)
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // 전체 경로에 적용 → Swagger + API 모두 CORS 허용
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}