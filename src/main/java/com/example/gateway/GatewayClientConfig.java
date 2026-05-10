package com.example.gateway;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class GatewayClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayClientConfig.class);

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory() {
            @Override
            @SuppressWarnings("deprecation")
            protected RequestConfig createRequestConfig(Object clientConfiguration) {
                RequestConfig config = super.createRequestConfig(clientConfiguration);

                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    Integer respTimeout = (Integer) attrs.getAttribute("custom-response-timeout", 0);
                    Integer connTimeout = (Integer) attrs.getAttribute("custom-connect-timeout", 0);
                    int safeRespTimeout = respTimeout;
                    int safeConnTimeout = connTimeout;

                    if (safeRespTimeout > 0) {
                        LOGGER.info("Applying timeouts: responseTimeout={}ms, connectTimeout={}ms",
                                safeRespTimeout, safeConnTimeout);
                        assert config != null;
                        config = RequestConfig.copy(config)
                                .setResponseTimeout(Timeout.ofMilliseconds(safeRespTimeout))
                                .setConnectTimeout(Timeout.ofMilliseconds(safeConnTimeout))
                                .build();
                    }
                }
                return config;
            }
        };
    }
}