package com.raisin.repository;

import static com.raisin.dto.record.Status.OK;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.raisin.dto.record.RecordA;
import com.raisin.dto.record.RecordB;
import com.raisin.util.HttpErrorHandler;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ReactiveSourceRepository implements SourceRepository {
  private static final RecordA RECORD_A_FALLBACK = new RecordA(Optional.empty(), OK);
  private static final RecordB RECORD_B_FALLBACK = new RecordB(Optional.empty(), Optional.empty());
  private static final Logger log = LoggerFactory.getLogger(ReactiveSourceRepository.class);
  private final WebClient webClient;
  private final ObjectMapper jsonMapper;
  private final XmlMapper xmlMapper;
  private final HttpErrorHandler httpErrorHandler;

  public ReactiveSourceRepository(WebClient webClient, ObjectMapper jsonMapper,
                                  XmlMapper xmlMapper, HttpErrorHandler httpErrorHandler) {
    this.webClient = webClient;
    this.jsonMapper = jsonMapper;
    this.xmlMapper = xmlMapper;
    this.httpErrorHandler = httpErrorHandler;
  }

  @Override
  public Mono<RecordA> callSourceA() {
    return callPath("/source/a", RecordA.class, RECORD_A_FALLBACK, jsonMapper);
  }

  @Override
  public Mono<RecordB> callSourceB() {
    return callPath("/source/b", RecordB.class, RECORD_B_FALLBACK, xmlMapper);
  }

  private <T> Mono<T> callPath(String uri, Class<T> clazz, T fallback, ObjectMapper objectMapper) {
    return webClient.get()
        .uri(uri)
        .retrieve()
        .onStatus(HttpStatusCode::isError, httpErrorHandler::handle)
        .bodyToMono(String.class)
        .map((body) -> deserialize(body, clazz, fallback, objectMapper))
        .doOnNext(body -> log.debug("{} -> {}", uri, body));
  }

  private <T> T deserialize(String body, Class<T> clazz, T fallback, ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(body, clazz);
    } catch (JacksonException e) {
      log.warn("Deserialization failed for message {}", body);
      return fallback;
    }
  }
}
