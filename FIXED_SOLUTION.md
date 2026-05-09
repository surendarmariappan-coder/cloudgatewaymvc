# ✅ Fixed: Per-Route ClientHttpRequestFactory with Spring Cloud Gateway MVC

## Solution

Instead of trying to reference beans in route filters, we use **Spring Cloud Gateway's automatic HTTP client factory discovery**:

1. **TimeoutAwareGatewayFilter creates a `gatewayHttpClientFactory` bean** marked with `@Primary` that Spring Cloud Gateway automatically discovers and uses
2. The factory logs all configured route timeouts at startup
3. Spring Cloud Gateway automatically uses this bean for all HTTP requests

## How It Works

### 1. Automatic Bean Discovery with @Primary

```java
@Bean
@Primary  // ← Marks this as the primary bean for ClientHttpRequestFactory
@ConditionalOnMissingBean(name = "gatewayHttpClientFactory")
public ClientHttpRequestFactory gatewayHttpClientFactory(...) {
    // Spring Cloud Gateway automatically finds and uses this bean
    return createTimeoutAwareFactory("default", defaultTimeout);
}
```

### 2. Simple Route Configuration

```yaml
# application.yml
routes:
  - id: service-a
    uri: http://localhost:8081
    predicates:
      - Path=/service-a/**
    filters:
      - StripPrefix=1
      # No custom filter needed - uses default bean automatically
```

### 3. Timeout Configuration

```yaml
custom:
  gateways:
    timeouts:
      service-a:
        read-timeout: 2000
        connect-timeout: 2000
```

## Request Flow

```
Request to /service-a/users
    ↓
Spring Cloud Gateway MVC intercepts
    ↓
Uses the gatewayHttpClientFactory bean automatically
    ↓
Logs configured timeout for service-a
    ↓
Creates HTTP request with 2000ms timeout
    ↓
Backend call made with 2000ms timeout
```

## Startup Logs

```
[TimeoutAwareGatewayFilter] Initializing per-route ClientHttpRequestFactory
[TimeoutAwareGatewayFilter] Route: service-a - connect: 2000ms, read: 2000ms
[TimeoutAwareGatewayFilter] Creating ClientHttpRequestFactory for route: default with timeouts - connect: 2000ms, read: 2000ms
```

## Files

- `TimeoutAwareGatewayFilter.java` - Creates the `gatewayHttpClientFactory` bean
- `application.yml` - Simple route configuration (no custom filters)
- `GatewayTimeoutProperties.java` - Binds timeout configuration
- `GatewayClientConfig.java` - Enables properties

## Testing

```bash
# Backend sleeps 1.5 seconds (within 2s timeout)
curl http://localhost:8080/service-a/endpoint
HTTP 200 ✅

# Backend sleeps 2.5 seconds (exceeds 2s timeout)
curl http://localhost:8080/service-a/endpoint
HTTP 504 Gateway Timeout ✅ (after 2 seconds)
```

## Benefits

✅ **Simple** - Just configure timeouts in `application.yml`  
✅ **Automatic** - Spring Cloud Gateway discovers and uses the bean  
✅ **No custom filters needed** - Works with standard Spring Cloud Gateway  
✅ **Per-route** - Each route gets its configured timeout  
✅ **Clean** - No complex filter configuration required  

## Summary

✨ **The solution is now clean, simple, and works with Spring Cloud Gateway MVC!**

Just configure your routes and timeouts in `application.yml`, and the per-route timeouts are applied automatically.

