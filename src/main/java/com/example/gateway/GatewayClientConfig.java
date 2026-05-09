package com.example.gateway;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway configuration for Spring Cloud Gateway WebMVC
 * Enables per-route timeout configuration via TimeoutAwareHttpClientFactory
 */
@Configuration
@EnableConfigurationProperties(GatewayTimeoutProperties.class)
public class GatewayClientConfig {
    // TimeoutAwareHttpClientFactory is auto-wired and available as a bean
    // Use it to get timeout-aware RestTemplate instances per route
}

