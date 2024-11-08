package com.raisin.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Kind {
  @JsonProperty("joined")
  JOINED(),
  @JsonProperty("orphaned")
  ORPHANED(),
  @JsonProperty("defective")
  DEFECTIVE()
}
