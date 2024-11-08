package com.raisin.cache;

import com.raisin.dto.record.RecordA;
import com.raisin.dto.record.RecordB;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SourceCache {
  private final Object DUMMY_VALUE = new Object();
  private final Map<String, Object> recordsA;
  private final Map<String, Object> recordsB;

  public SourceCache() {
    recordsA = new ConcurrentHashMap<>(32_768);
    recordsB = new ConcurrentHashMap<>(32_768);
  }

  public Set<String> getIdsForRecordsA() {
    return Collections.unmodifiableSet(recordsA.keySet());
  }

  public Set<String> getIdsForRecordsB() {
    return Collections.unmodifiableSet(recordsB.keySet());
  }

  public boolean containsIdInRecordsA(String id) {
    return recordsA.containsKey(id);
  }

  public boolean containsIdInRecordsB(String id) {
    return recordsB.containsKey(id);
  }

  public void storeInCache(RecordA record) {
    record.id().ifPresent(id -> recordsA.put(id.intern(), DUMMY_VALUE));
  }

  public void storeInCache(RecordB record) {
    record.value().ifPresent(id -> recordsB.put(id.intern(), DUMMY_VALUE));
  }

  public void evictFromCache(String id) {
    recordsA.remove(id);
    recordsB.remove(id);
  }
}
