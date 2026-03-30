package de.clsg.recap_springboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import de.clsg.recap_springboot.dto.GptMessageDto;
import de.clsg.recap_springboot.dto.GptResponseDto;
import de.clsg.recap_springboot.dto.GptRequestDto;

@Service
public class SpellingService {
  private final RestClient restClient;

  public SpellingService(@Qualifier("openAiRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  public String checkSpelling(String description) {
    GptRequestDto req = new GptRequestDto(
      "gpt-5.4",
      List.of(new GptMessageDto(buildDescriptionPrompt(description), "developer"))
    );

    GptResponseDto resp = restClient.post()
      .contentType(MediaType.APPLICATION_JSON)
      .body(req)
      .retrieve()
      .body(GptResponseDto.class);

    return resp.choices().getFirst().message().content();
  }

  private String buildDescriptionPrompt(String description) {
    return "Do not return any alternative suggestions. Only correct spelling and return the same string in the original language: " + description;
  }
}
