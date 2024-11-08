package com.raisin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.raisin.cache.SourceCache;
import com.raisin.config.ObjectMapperFactory;
import com.raisin.config.WebClientFactory;
import com.raisin.repository.ReactiveSinkRepository;
import com.raisin.repository.ReactiveSourceRepository;
import com.raisin.repository.SinkRepository;
import com.raisin.repository.SourceRepository;
import com.raisin.service.ReactiveProcessingService;
import com.raisin.util.HttpErrorHandler;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * TODO: Create a Guice module
 */
public class ApplicationModule {
  private static HttpErrorHandler httpErrorHandler() {
    return new HttpErrorHandler();
  }

  public SourceRepository sourceRepository() {
    var xmlMapper = xmlObjectMapper();
    var jsonMapper = jsonObjectMapper();
    var webClient = webClient(jsonMapper);
    var httpErrorHandler = httpErrorHandler();
    return new ReactiveSourceRepository(webClient, jsonMapper, xmlMapper, httpErrorHandler);
  }

  public SinkRepository sinkRepository() {
    var jsonMapper = jsonObjectMapper();
    var webClient = webClient(jsonMapper);
    var httpErrorHandler = httpErrorHandler();
    return new ReactiveSinkRepository(webClient, httpErrorHandler);
  }

  public WebClient webClient(ObjectMapper jsonMapper) {
    return WebClientFactory.create(jsonMapper);
  }

  public ObjectMapper jsonObjectMapper() {
    return ObjectMapperFactory.createObjectMapper();
  }

  public XmlMapper xmlObjectMapper() {
    return ObjectMapperFactory.createXmlObjectMapper();
  }

  public SourceCache sourceCache() {
    return new SourceCache();
  }

  public ReactiveProcessingService reactiveProcessingService() {
    var sourceRepository = sourceRepository();
    var sinkRepository = sinkRepository();
    var sourceCache = sourceCache();
    return new ReactiveProcessingService(sourceRepository, sinkRepository, sourceCache);
  }
}
