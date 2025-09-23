package com.deliveranything.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

  @Bean
  public WebClient osrmWebClient(
      // application.yml에 설정된 OSRM 서버 URL을 주입
      // 배포 시 http://{EC2-IP}:5000 형태로 변경 필요
      @Value("${osrm.base-url:http://localhost:5000}") String baseUrl) {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }
}
