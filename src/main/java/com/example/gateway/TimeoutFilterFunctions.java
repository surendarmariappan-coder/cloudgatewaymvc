package com.example.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.gateway.server.mvc.common.Shortcut;
import org.springframework.cloud.gateway.server.mvc.filter.FilterSupplier;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class TimeoutFilterFunctions {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutFilterFunctions.class);

    @Shortcut({"responseTimeout", "connectTimeout"})
    public static HandlerFilterFunction<ServerResponse, ServerResponse> timeout(int responseTimeout, int connectTimeout) {
        return (request, next) -> {
            HttpServletRequest httpServletRequest = request.servletRequest();
            LOGGER.info("Applying timeouts1: responseTimeout={}ms, connectTimeout={}ms", responseTimeout, connectTimeout);
            // Save to request attributes for the ClientHttpRequestFactory to find
            httpServletRequest.setAttribute("custom-response-timeout", responseTimeout);
            httpServletRequest.setAttribute("custom-connect-timeout", connectTimeout);
            return next.handle(request);
        };
    }

    public static class Supplier implements FilterSupplier {
        @Override
        public Collection<Method> get() {
            try {
                return List.of(TimeoutFilterFunctions.class.getMethod("timeout", int.class, int.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}