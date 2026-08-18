package com.ecc.job.config;

import java.time.Duration;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Provides the {@link RestTemplate} used to call downstream microservices, so it's built once via Spring's auto-configured
 * {@link RestTemplateBuilder} instead of each caller constructing its own client.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates the shared {@link RestTemplate} bean.
     *
     * @param builder Spring Boot's auto-configured builder
     * @return a {@link RestTemplate} with a 5-second connect timeout and a 10-second read timeout
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.connectTimeout(Duration.ofSeconds(5)).readTimeout(Duration.ofSeconds(10)).build();
    }
}
