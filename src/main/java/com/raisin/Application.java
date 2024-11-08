package com.raisin;

public class Application {
  public static void main(String[] args) {
    var appModule = new ApplicationModule();
    var reactiveProcessingService = appModule.reactiveProcessingService();
    reactiveProcessingService.processRecords();
  }
}
