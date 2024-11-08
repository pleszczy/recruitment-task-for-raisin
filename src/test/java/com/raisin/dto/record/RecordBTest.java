package com.raisin.dto.record;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.raisin.config.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

class RecordBTest {
  @Test
  void should_be_deserializable() throws JsonProcessingException {
    var xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <msg>
            <id value="04dab73931c1bbc8d7f6fb689ccd48fc"/>
        </msg>""";
    var xmlMapper = ObjectMapperFactory.createXmlObjectMapper();

    var actual = xmlMapper.readValue(xml, RecordB.class);

    assertAll(
        () -> assertThat(actual.value()).hasValue("04dab73931c1bbc8d7f6fb689ccd48fc"),
        () -> assertThat(actual.done()).isNotPresent()
    );
  }

  @Test
  void should_be_deserializable_when_done() throws JsonProcessingException {
    var xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <msg>
            <done/>
        </msg>""";
    var xmlMapper = ObjectMapperFactory.createXmlObjectMapper();

    var actual = xmlMapper.readValue(xml, RecordB.class);

    assertThat(actual.done()).isPresent();
  }
}