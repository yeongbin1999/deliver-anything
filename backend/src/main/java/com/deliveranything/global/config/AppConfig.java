package com.deliveranything.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

  @Bean
  public WebClient osrmWebClient(
      // application.yml에 설정된 osrm.base-url 값을 주입, 기본값은 http://localhost:5000
      @Value("${osrm.base-url:http://localhost:5000}") String baseUrl) {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}