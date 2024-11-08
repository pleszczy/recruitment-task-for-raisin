package com.raisin.service;

import static com.raisin.dto.record.Kind.JOINED;
import static com.raisin.dto.record.Kind.ORPHANED;
import static com.raisin.dto.record.Status.DONE;
import static com.raisin.dto.record.Status.OK;

import com.raisin.cache.SourceCache;
import com.raisin.dto.record.Id;
import com.raisin.dto.record.RecordA;
import com.raisin.dto.record.RecordB;
import com.raisin.dto.record.RecordSink;
import com.raisin.repository.SinkRepository;
import com.raisin.repository.SourceRepository;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveProcessingService {
  public static final Tuple2<RecordA, RecordB> EMPTY_RESULT = Tuples.of(
      new RecordA(Optional.empty(), OK),
      new RecordB(Optional.empty(), Optional.empty())
  );
  private static final Logger log = LoggerFactory.getLogger(ReactiveProcessingService.class);
  private final SourceRepository sourceRepository;
  private final SinkRepository sinkRepository;
  private final SourceCache sourceCache;

  public ReactiveProcessingService(SourceRepository sourceRepository, SinkRepository sinkRepository, SourceCache sourceCache) {
    this.sourceRepository = sourceRepository;
    this.sinkRepository = sinkRepository;
    this.sourceCache = sourceCache;
  }

  public void processRecords() {
    Mono.zip(sourceRepository.callSourceA(), sourceRepository.callSourceB())
        .expand(result -> Mono.zipDelayError(sourceRepository.callSourceA(), sourceRepository.callSourceB()))
        .takeUntil(result -> Objects.equals(result.getT1().status(), DONE) && result.getT2().done().isPresent())
        .doOnNext(this::storeResultsInCache)
        .flatMap(this::processInOrderRecords)
        .flatMap(this::processOutOfOrderRecordA)
        .flatMap(this::processOutOfOrderRecordB)
        .blockLast();

    // TODO: This is a non-optimal approach for handling orphans. Will work till around 100k records (when running against localhost server)
    processOrphanedRecords();
  }

  private void storeResultsInCache(Tuple2<RecordA, RecordB> result) {
    sourceCache.storeInCache(result.getT1());
    sourceCache.storeInCache(result.getT2());
  }

  private Mono<Tuple2<RecordA, RecordB>> processOutOfOrderRecordB(Tuple2<RecordA, RecordB> results) {
    if (results == EMPTY_RESULT) {
      return Mono.just(EMPTY_RESULT);
    }

    var recordB = results.getT2();
    if (recordB.id().isPresent()) {
      var id = recordB.id().get().value();
      if (sourceCache.containsIdInRecordsA(id)) {
        sourceCache.evictFromCache(id);
        return sinkRepository.callSinkA(new RecordSink(JOINED, id))
            .thenReturn(results);
      }
    }
    return Mono.just(results);
  }

  private Mono<Tuple2<RecordA, RecordB>> processOutOfOrderRecordA(Tuple2<RecordA, RecordB> results) {
    if (results == EMPTY_RESULT) {
      return Mono.just(EMPTY_RESULT);
    }

    var recordA = results.getT1();
    if (recordA.id().isPresent()) {
      var id = recordA.id().get();
      if (sourceCache.containsIdInRecordsB(id)) {
        sourceCache.evictFromCache(id);
        return sinkRepository.callSinkA(new RecordSink(JOINED, id))
            .thenReturn(results);
      }
    }
    return Mono.just(results);
  }

  private Mono<Tuple2<RecordA, RecordB>> processInOrderRecords(Tuple2<RecordA, RecordB> results) {
    var recordA = results.getT1();
    var recordB = results.getT2();
    if (areRecordsJoined(recordA, recordB)) {
      recordA.id().ifPresent(sourceCache::evictFromCache);
      return sinkRepository.callSinkA(new RecordSink(JOINED, recordA.id().orElseThrow()))
          .thenReturn(EMPTY_RESULT);
    }
    return Mono.just(results);
  }

  private void processOrphanedRecords() {
    Flux.merge(
            Flux.fromIterable(sourceCache.getIdsForRecordsA())
                .flatMap(key -> sinkRepository.callSinkA(new RecordSink(ORPHANED, key))),
            Flux.fromIterable(sourceCache.getIdsForRecordsB())
                .flatMap(key -> sinkRepository.callSinkA(new RecordSink(ORPHANED, key)))
        )
        .doOnTerminate(() -> log.info("Completed processing all orphans"))
        .blockLast(Duration.ofSeconds(20));
  }

  private boolean areRecordsJoined(RecordA recordA, RecordB recordB) {
    return recordA.id().isPresent()
           && recordB.id().isPresent()
           && Objects.equals(recordA.id().get(), recordB.id().map(Id::value).orElseThrow())
           && recordA.status() != DONE
           && recordB.done().isEmpty();
  }
}
