package com.raisin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public class HttpErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(HttpErrorHandler.class);

  public Mono<? extends Throwable> handle(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(body -> {
          log.warn("[{}] status: {}, body: {}", response.request().getURI(), response.statusCode(), body);
          return Mono.empty();
        });
  }
}
