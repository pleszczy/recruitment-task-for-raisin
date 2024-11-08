package com.raisin.dto.record;

import java.util.Optional;

public record RecordB(Optional<Id> id, Optional<Done> done) {
  public Optional<String> value() {
    return id.map(Id::value);
  }
}

