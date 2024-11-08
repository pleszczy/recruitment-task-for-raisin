package com.raisin.repository;

import com.raisin.dto.Response;
import com.raisin.dto.record.RecordSink;
import com.raisin.util.HttpErrorHandler;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class ReactiveSinkRepository implements SinkRepository {
  private static final Logger log = LoggerFactory.getLogger(ReactiveSinkRepository.class);
  private final WebClient webClient;
  private final HttpErrorHandler httpErrorHandler;

  public ReactiveSinkRepository(WebClient webClient, HttpErrorHandler httpErrorHandler) {
    this.webClient = webClient;
    this.httpErrorHandler = httpErrorHandler;
  }

  @Override
  public Mono<Response> callSinkA(RecordSink sinkRecord) {
    return webClient.post()
        .uri("/sink/a")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(sinkRecord), RecordSink.class)
        .retrieve()
        .onStatus(HttpStatusCode::isError, httpErrorHandler::handle)
        .bodyToMono(Response.class)
        .retryWhen(Retry.fixedDelay(200, Duration.ofMillis(600))
            .doAfterRetry(retry -> log.info("[sink/a] Retrying {} times for {}", retry.totalRetries(), sinkRecord)))
        .onErrorContinue((e, obj) -> log.error("[sink/a] Could not post {} because of {}", sinkRecord, e.getMessage()))
        .doOnNext(body -> log.info("[sink/a] {} -> {}", sinkRecord, body));
  }
}
