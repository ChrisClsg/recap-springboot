package de.clsg.recap_springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@SpringBootTest
@AutoConfigureMockRestServiceServer
public class SpellingServiceTest {
  @Autowired private SpellingService spellingService;
  @Autowired private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer.reset();
  }

  @Test
  void checkSpelling_returnsCorrectedDescription_andSendsExpectedRequest() {
    mockServer.expect(requestTo("https://api.openai.com/v1/chat/completions"))
      .andExpect(method(HttpMethod.POST))
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json("""
        {
          "model": "gpt-5.4",
          "messages": [
            {
              "content": "Do not return any alternative suggestions. Only correct spelling and return the same string in the original language: Helo Worl",
              "role": "developer"
            }
          ]
        }
        """, true))
      .andRespond(withSuccess("""
        {
          "choices": [
            {
              "message": {
                "content": "Hello World"
              }
            }
          ]
        }
        """, MediaType.APPLICATION_JSON));

    String result = spellingService.checkSpelling("Helo Worl");

    assertEquals("Hello World", result);
    mockServer.verify();
  }
}
