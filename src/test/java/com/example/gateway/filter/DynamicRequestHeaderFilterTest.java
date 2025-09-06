package com.example.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicRequestHeaderFilterTest {

    @Test
    void addsDynamicCustomHeaderToRequest() throws ServletException, IOException {
        DynamicRequestHeaderFilter filter = new DynamicRequestHeaderFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        final HttpServletRequest[] seen = new HttpServletRequest[1];

        FilterChain chain = (req, res) -> {
            seen[0] = (HttpServletRequest) req;
        };

        filter.doFilter(request, response, chain);

        assertThat(seen[0]).isNotNull();
        String header = seen[0].getHeader(DynamicRequestHeaderFilter.X_CUSTOM_HEADER);
        assertThat(header).isNotBlank();

        // Ensure header is listed among header names
        boolean found = false;
        Enumeration<String> names = seen[0].getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            if (DynamicRequestHeaderFilter.X_CUSTOM_HEADER.equalsIgnoreCase(n)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }
}
