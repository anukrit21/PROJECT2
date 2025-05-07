package com.demoApp.mess.config;

import feign.Retryer;
import feign.Target;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients(basePackages = "com.demoApp.mess.client")
public class FeignClientConfig {

    @Bean
    public Retryer feignRetryer() {
        // Retry up to 3 times with 100ms initial backoff, multiplied by 1.5 each attempt
        return new Retryer.Default(100, TimeUnit.MILLISECONDS.toMillis(1000), 3);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (String feignClientName, Target<?> target, Method method) -> feignClientName;
    }

    /**
     * Custom error decoder that logs Feign client errors and provides better error handling
     */
    static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // Log the error
            System.err.println("Error while calling Feign client: " + methodKey +
                    ", status: " + response.status() +
                    ", reason: " + response.reason());

            // Return the default implementation
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
