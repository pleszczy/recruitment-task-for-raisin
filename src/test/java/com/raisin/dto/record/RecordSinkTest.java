package com.raisin.dto.record;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.raisin.config.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

class RecordSinkTest {
  @Test
  void should_be_serializable() throws JsonProcessingException {
    var record = new RecordSink(Kind.ORPHANED, "0ff07739-f309-4fbd-8067-bfc803d26ce9");
    var objectMapper = ObjectMapperFactory.createObjectMapper();

    var actual = objectMapper.writeValueAsString(record);

    assertThat(actual).isEqualTo("""
        {"kind":"orphaned","id":"0ff07739-f309-4fbd-8067-bfc803d26ce9"}""");
  }
}