package com.raisin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

public final class WebClientFactory {
  public static WebClient create(ObjectMapper jsonMapper) {
    return WebClient.builder()
        // TODO: Extract configuration
        .baseUrl("http://localhost:7299")
        .codecs(configurer -> {
              configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(jsonMapper, MediaType.APPLICATION_JSON));
              configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(jsonMapper, MediaType.APPLICATION_JSON));
            }
        ).build();
  }
}
