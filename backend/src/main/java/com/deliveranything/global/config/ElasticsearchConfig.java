package com.deliveranything.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

@Configuration
@EnableElasticsearchRepositories
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Override
  @NonNull
  public ClientConfiguration clientConfiguration() {
    String host = "localhost:9200";
    return ClientConfiguration.builder()
        .connectedTo(host)
        .build();
  }
}
