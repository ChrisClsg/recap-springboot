package de.clsg.recap_springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient openAiRestClient(
    @Value("${openai.api-key:}") String apiKey,
    RestClient.Builder builder
  ) {
    return builder
      .baseUrl("https://api.openai.com/v1/chat/completions")
      .defaultHeader("Authorization", "Bearer " + apiKey)
      .build();
  }
}
