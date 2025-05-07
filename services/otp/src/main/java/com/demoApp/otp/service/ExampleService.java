package com.demoApp.otp.service;

import com.demoApp.otp.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    private final AppProperties appProperties;

    public ExampleService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void printAppName() {
        System.out.println("Application Name: " + appProperties.getName());
    }
}
