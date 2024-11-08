package com.raisin.dto.record;

import static com.raisin.dto.record.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.raisin.config.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

class RecordATest {

  @Test
  void should_be_deserializable() throws JsonProcessingException {
    var json = """
        {
          "status": "ok",
          "id": "5830372d41d1321b9ce2c18beb6310e9"
        }""";
    var objectMapper = ObjectMapperFactory.createObjectMapper();

    var actual = objectMapper.readValue(json, RecordA.class);

    assertAll(
        () -> assertThat(actual.status()).isEqualTo(OK),
        () -> assertThat(actual.id()).hasValue("5830372d41d1321b9ce2c18beb6310e9")
    );
  }
}