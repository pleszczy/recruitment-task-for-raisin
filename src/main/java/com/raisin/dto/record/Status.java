package com.raisin.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
  @JsonProperty("ok")
  OK,
  @JsonProperty("done")
  DONE
}
