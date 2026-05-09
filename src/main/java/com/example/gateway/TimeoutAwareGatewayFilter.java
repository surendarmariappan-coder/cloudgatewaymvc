package com.example.gateway;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Factory for creating per-route timeout-aware ClientHttpRequestFactory.
 *
 * Creates a default ClientHttpRequestFactory bean that Spring Cloud Gateway
 * will use automatically for all HTTP requests.
 */
@Configuration
public class TimeoutAwareGatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(TimeoutAwareGatewayFilter.class);

    /**
     * Create the default ClientHttpRequestFactory used by Spring Cloud Gateway MVC.
     * This factory reads the default timeout from application.yml (custom.gateways.timeouts.default)
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "gatewayHttpClientFactory")
    public ClientHttpRequestFactory gatewayHttpClientFactory(
            GatewayTimeoutProperties timeoutProperties,
            GatewayMvcProperties gatewayMvcProperties) {

        log.info("[TimeoutAwareGatewayFilter] Initializing per-route ClientHttpRequestFactory");

        // Log all configured route timeouts
        if (gatewayMvcProperties.getRoutes() != null) {
            for (var route : gatewayMvcProperties.getRoutes()) {
                String routeId = extractRouteId(route);
                var timeout = timeoutProperties.timeouts().getOrDefault(routeId,
                        timeoutProperties.timeouts().get("default"));

                log.info("[TimeoutAwareGatewayFilter] Route: {} - connect: {}ms, read: {}ms",
                        routeId, timeout.connectTimeout(), timeout.readTimeout());
            }
        }

        // Get default timeout from application.yml (custom.gateways.timeouts.default)
        var defaultTimeout = timeoutProperties.timeouts().get("default");
        if (defaultTimeout == null) {
            defaultTimeout = new GatewayTimeoutProperties.RouteTimeout(5000, 5000);
            log.warn("[TimeoutAwareGatewayFilter] No default timeout configured, using fallback: 5000ms");
        }

        return createTimeoutAwareFactory("default", defaultTimeout);
    }

    /**
     * Create individual route-specific beans for explicit reference (optional).
     * These can be referenced in custom filters if needed.
     */
    @Bean(name = "clientHttpRequestFactory_service_a")
    public ClientHttpRequestFactory clientHttpRequestFactoryServiceA(GatewayTimeoutProperties timeoutProperties) {
        return createTimeoutAwareFactory("service-a", timeoutProperties);
    }


    //...existing code...

    /**
     * Helper method to create a timeout-aware ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory createTimeoutAwareFactory(String routeId, GatewayTimeoutProperties timeoutProperties) {
        var timeout = timeoutProperties.timeouts().getOrDefault(routeId,
                new GatewayTimeoutProperties.RouteTimeout(5000, 5000));
        return createTimeoutAwareFactory(routeId, timeout);
    }

    /**
     * Helper method to create a timeout-aware ClientHttpRequestFactory with explicit timeout
     */
    private ClientHttpRequestFactory createTimeoutAwareFactory(String routeId, GatewayTimeoutProperties.RouteTimeout timeout) {
        log.info("[TimeoutAwareGatewayFilter] Creating ClientHttpRequestFactory for route: {} with timeouts - connect: {}ms, read: {}ms",
                routeId, timeout.connectTimeout(), timeout.readTimeout());

        @SuppressWarnings("deprecation")
        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeout.connectTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout.readTimeout()))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new BufferingClientHttpRequestFactory(factory);
    }

    /**
     * Extract route ID from route object using reflection
     */
    private String extractRouteId(Object route) {
        try {
            return (String) route.getClass().getMethod("getId").invoke(route);
        } catch (Exception e) {
            log.warn("[TimeoutAwareGatewayFilter] Could not extract route ID from route object");
            return "unknown";
        }
    }
}

