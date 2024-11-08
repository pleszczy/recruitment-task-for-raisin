package com.raisin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public final class ObjectMapperFactory {
  public static ObjectMapper createObjectMapper() {
    return JsonMapper.builder()
        .addModule(new Jdk8Module())
        .build();
  }

  public static XmlMapper createXmlObjectMapper() {
    return XmlMapper.builder()
        .addModule(new Jdk8Module())
        .addModule(new ParameterNamesModule())
        .addModule(new Jdk8Module())
        .build();
  }
}
