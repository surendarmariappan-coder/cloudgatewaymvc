package com.example.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Servlet filter that injects dynamic headers into the incoming request so that
 * Spring Cloud Gateway MVC forwards them to downstream services.
 *
 * This adds:
 * - x-custom-header: a per-request UUID value
 */
@Component
public class DynamicRequestHeaderFilter extends OncePerRequestFilter {

    public static final String X_CUSTOM_HEADER = "x-custom-header";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generate a dynamic value for x-custom-header for each request
        String dynamicValue = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(X_CUSTOM_HEADER, dynamicValue);
        headers.put("Content-Type", "application/json");

        HttpServletRequest wrapped = new AddHeaderRequestWrapper(request, headers);
        filterChain.doFilter(wrapped, response);
    }

    /**
     * Request wrapper that augments headers with additional entries supplied at construction.
     */
    static class AddHeaderRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> extraHeaders;

        AddHeaderRequestWrapper(HttpServletRequest request, Map<String, String> extraHeaders) {
            super(request);
            // store with case-insensitive keys to align with HTTP header semantics
            Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            if (extraHeaders != null) {
                map.putAll(extraHeaders);
            }
            this.extraHeaders = Collections.unmodifiableMap(map);
        }

        @Override
        public String getHeader(String name) {
            String value = extraHeaders.get(name);
            if (value != null) {
                return value;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = new ArrayList<>();
            if (extraHeaders.containsKey(name)) {
                values.add(extraHeaders.get(name));
            }
            Enumeration<String> original = super.getHeaders(name);
            while (original.hasMoreElements()) {
                values.add(original.nextElement());
            }
            return Collections.enumeration(values);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            names.addAll(extraHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
