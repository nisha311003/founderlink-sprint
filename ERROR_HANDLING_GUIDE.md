# API Gateway Error Handling Guide

## Overview
This document describes the global exception handling mechanism implemented in the FounderLink API Gateway to provide consistent, user-friendly error responses across all microservices.

## Features

### 1. **Service Unavailability Handling**
When a microservice (e.g., StartupService, UserService, AuthService) is unavailable or not registered in Eureka, the gateway returns a **503 Service Unavailable** response:

```json
{
  "error": "Service Unavailable",
  "message": "The requested microservice is currently unavailable. Please try again later.",
  "status": 503,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

**Common Causes:**
- Microservice is down or not running
- Service is not registered with Eureka Server
- Service registration has expired
- Network connectivity issues between gateway and service

**Resolution Steps:**
1. Check if the microservice is running: `curl http://localhost:8083/actuator/health`
2. Verify Eureka registration: `curl http://localhost:8761/eureka/apps`
3. Check service logs for startup errors
4. Restart the microservice if needed

### 2. **Not Found (404) Handling**
When requesting a non-existent endpoint or resource:

```json
{
  "error": "Not Found",
  "message": "The requested resource was not found",
  "status": 404,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/users/999"
}
```

### 3. **Bad Request (400) Handling**
When sending invalid request parameters:

```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "status": 400,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/auth/login"
}
```

### 4. **Internal Server Error (500) Handling**
For unexpected server-side errors:

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact support if the problem persists.",
  "status": 500,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

## Implementation Details

### GlobalExceptionHandler.java
Located at: `apiGateway/src/main/java/com/founderlink/apiGateway/exception/GlobalExceptionHandler.java`

**Key Components:**

#### 1. **NotFoundException Detection**
```java
if (ex instanceof NotFoundException notFoundException) {
    String exceptionMessage = notFoundException.getMessage();
    
    if (exceptionMessage != null && exceptionMessage.contains("Unable to find instance")) {
        // Service Unavailable - 503
    } else {
        // Not Found - 404
    }
}
```

The handler distinguishes between:
- **Service unavailable**: "Unable to find instance for SERVICENAME"
- **Resource not found**: Other NotFoundException scenarios

#### 2. **Error Response Building**
Uses `ObjectMapper` for proper JSON serialization with the following fields:
- `error`: Error type/category
- `message`: Human-readable error message
- `status`: HTTP status code
- `timestamp`: ISO 8601 formatted timestamp
- `path`: The requested endpoint path

#### 3. **Logging**
Structured logging provides visibility into errors:
```
[GATEWAY ERROR] Status: 503 Service Unavailable | Error: Service Unavailable | Message: The requested microservice is currently unavailable. Please try again later. | Details: 503 SERVICE_UNAVAILABLE "Unable to find instance for STARTUPSERVICE"
```

#### 4. **Content-Type Header**
All error responses automatically include:
```
Content-Type: application/json
```

## Microservices Configuration

### Service Port Mapping
- **EurekaServer**: Port 8761
- **AuthService**: Port 8081
- **UserService**: Port 8082
- **StartupService**: Port 8083
- **ApiGateway**: Port 8080

### Gateway Routes
The API Gateway defines the following routes:

```yaml
POST   /founderlink/auth/**      → AUTHSERVICE (/api/auth/*)
GET    /founderlink/users/**     → USERSERVICE (/api/users/*)
POST   /founderlink/startups/**  → STARTUPSERVICE (/api/startups/*)
```

## Troubleshooting

### Issue: 503 Service Unavailable

**Diagnostic Steps:**
```bash
# 1. Check if the service is running
curl http://localhost:8083/actuator/health

# 2. Check Eureka registration
curl http://localhost:8761/eureka/apps

# 3. Verify service configuration
grep -r "eureka.client.service-url" startupService/

# 4. Check network connectivity
ping localhost
```

**Common Solutions:**
1. **Service not started**: Start the service using `mvn spring-boot:run` or the JAR file
2. **Eureka not running**: Ensure Eureka Server is running on port 8761
3. **Service registration failed**: Check application logs for initialization errors
4. **Port conflict**: Verify the port (8083 for StartupService) is not in use

### Issue: Frequent 503 Errors for a Specific Service

This usually indicates:
1. **Service crashes**: Check service logs for runtime exceptions
2. **Resource exhaustion**: Monitor CPU, memory, and database connections
3. **Database connectivity**: Verify database credentials and availability
4. **Configuration server**: Ensure CloudConfig server (port 8071) is accessible

### Monitoring

Monitor these endpoints for service health:
```bash
# Startup Service Health
curl http://localhost:8083/actuator/health

# Eureka Service Registry
curl http://localhost:8761/eureka/apps

# Gateway Metrics
curl http://localhost:8080/actuator/metrics
```

## Testing the Exception Handler

Run the test suite:
```bash
mvn test -Dtest=GlobalExceptionHandlerTest
```

### Manual Testing

**Test Service Unavailability:**
```bash
# 1. Stop StartupService
# 2. Make a request
curl -X POST http://localhost:8080/founderlink/startups \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-User-Email: test@test.com" \
  -d '{"name":"Test Startup"}'

# Expected Response:
# {
#   "error": "Service Unavailable",
#   "message": "The requested microservice is currently unavailable. Please try again later.",
#   "status": 503,
#   "timestamp": "2026-03-25T12:07:28",
#   "path": "/founderlink/startups"
# }
```

## Best Practices

### For API Consumers
1. **Implement retry logic** for 503 responses with exponential backoff
2. **Check error status codes** to differentiate between client and server errors
3. **Use the path field** for debugging and logging
4. **Monitor timestamps** to track error patterns over time

### For Developers
1. **Check gateway logs** first when services are unavailable
2. **Verify Eureka registration** before assuming service failure
3. **Use health endpoints** to diagnose service status
4. **Monitor metrics** to detect patterns and prevent issues

## Future Enhancements

Potential improvements to the exception handling:
1. **Rate limiting responses**: Return 429 Too Many Requests with retry-after header
2. **Circuit breaker pattern**: Prevent cascading failures
3. **Custom error codes**: Add application-specific error codes
4. **Distributed tracing**: Include request trace IDs for debugging
5. **Metrics collection**: Track error rates by service and endpoint
6. **Alert integration**: Notify operations team of persistent failures

## References

- [Spring Cloud Gateway Documentation](https://cloud.spring.io/spring-cloud-gateway/reference/html/)
- [Eureka Service Discovery](https://cloud.spring.io/spring-cloud-netflix/reference/html/#service-discovery-eureka-clients)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Reactive Exception Handling](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-errors)

