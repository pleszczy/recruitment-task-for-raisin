package com.raisin.repository;

import com.raisin.dto.Response;
import com.raisin.dto.record.RecordSink;
import reactor.core.publisher.Mono;

public interface SinkRepository {
  Mono<Response> callSinkA(RecordSink sinkRecord);
}
