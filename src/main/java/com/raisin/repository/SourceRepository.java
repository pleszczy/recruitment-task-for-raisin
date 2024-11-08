package com.raisin.repository;

import com.raisin.dto.record.RecordA;
import com.raisin.dto.record.RecordB;
import reactor.core.publisher.Mono;

public interface SourceRepository {
  Mono<RecordA> callSourceA();

  Mono<RecordB> callSourceB();
}
