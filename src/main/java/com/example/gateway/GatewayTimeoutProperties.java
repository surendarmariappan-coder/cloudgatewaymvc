package com.example.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "custom.gateways")
public record GatewayTimeoutProperties(Map<String, RouteTimeout> timeouts) {
    public record RouteTimeout(int readTimeout, int connectTimeout) {}
}
