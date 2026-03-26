# FounderLink Microservices Architecture & Error Handling

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Applications                      │
│                    (Web/Mobile/Desktop)                          │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                    HTTP/REST API Requests
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     API GATEWAY (Port 8080)                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Spring Cloud Gateway                                   │   │
│  │  - Route Management                                     │   │
│  │  - Filter Chain                                         │   │
│  │  - Service Discovery Integration                        │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  GlobalExceptionHandler (NEW)                           │   │
│  │  - Catches unhandled exceptions                         │   │
│  │  - Returns consistent error responses                   │   │
│  │  - Handles service unavailability (503)                 │   │
│  │  - Logs all errors with structured format              │   │
│  └─────────────────────────────────────────────────────────┘   │
└────┬──────────────┬──────────────┬──────────────────────────────┘
     │              │              │
     │ lb://        │ lb://        │ lb://
     │AUTHSERVICE   │USERSERVICE   │STARTUPSERVICE
     │              │              │
     ▼              ▼              ▼
┌─────────────┐┌─────────────┐┌──────────────┐
│ AUTH        ││ USER        ││ STARTUP      │
│ SERVICE     ││ SERVICE     ││ SERVICE      │
│             ││             ││              │
│ Port: 8081  ││ Port: 8082  ││ Port: 8083   │
│             ││             ││              │
│ Registered  ││ Registered  ││ Registered   │
│ with Eureka ││ with Eureka ││ with Eureka  │
└─────┬───────┘└─────┬───────┘└──────┬───────┘
      │              │               │
      │    Eureka Discovery          │
      │      (Port 8761)             │
      │         ┌─────────────────────┘
      └─────────┤
                │  Service Registry
                │  (Maintains list of
                │   active services)
                │
        ┌───────▼────────┐
        │ EUREKA SERVER  │
        │ Port: 8761     │
        │                │
        │ Service Names: │
        │ - AUTHSERVICE  │
        │ - USERSERVICE  │
        │ - STARTUPSERVICE
        │ - APIGATEWAY   │
        └────────────────┘

                                    Database Layer
                                    (MySQLx3)
                                    ┌─────────────────┐
                                    │ startup_db      │
                                    │ user_db         │
                                    │ auth_db         │
                                    └─────────────────┘
```

## Request Flow with Error Handling

### Scenario 1: Successful Request (200 OK)

```
Client Request
    │
    ▼
API Gateway
    │
    ├─ Route Matching: /founderlink/startups → lb://STARTUPSERVICE
    │
    ├─ Service Discovery: Query Eureka for STARTUPSERVICE instances
    │
    ├─ Load Balancing: Select available instance (localhost:8083)
    │
    ├─ Forward Request to StartupService
    │
    └─ Return Response with 200 OK
    │
    ▼
Client
```

### Scenario 2: Service Unavailable (503 Error)

```
Client Request
    │
    ▼
API Gateway
    │
    ├─ Route Matching: /founderlink/startups → lb://STARTUPSERVICE
    │
    ├─ Service Discovery: Query Eureka for STARTUPSERVICE instances
    │
    ├─ ❌ NO INSTANCES FOUND
    │   ↓
    ├─ Gateway throws: NotFoundException("Unable to find instance...")
    │
    ├─ GlobalExceptionHandler catches exception
    │
    ├─ Builds error response:
    │   {
    │     "error": "Service Unavailable",
    │     "message": "The requested microservice is currently unavailable...",
    │     "status": 503,
    │     "timestamp": "2026-03-25T12:07:28",
    │     "path": "/founderlink/startups"
    │   }
    │
    ├─ Sets HTTP Status: 503
    │
    ├─ Sets Content-Type: application/json
    │
    ├─ Logs error with structured format
    │
    └─ Return response to client
    │
    ▼
Client (receives 503)
```

## Configuration Files

### Application Ports

```yaml
# application.yaml files location and content

apiGateway/apiGateway/src/main/resources/application.yaml
  server.port: 8080
  eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/

authService/authService/src/main/resources/application.yaml
  server.port: 8081
  eureka.client.service-url.defaultZone: http://localhost:8761/eureka/

userService/userService/src/main/resources/application.yaml
  server.port: 8082
  eureka.client.service-url.defaultZone: http://localhost:8761/eureka/

startupService/startupService/src/main/resources/application.yaml
  server.port: 8083
  eureka.client.service-url.defaultZone: http://localhost:8761/eureka/
  spring.datasource.url: jdbc:mysql://localhost:3306/startup_db

eurekaServer/src/main/resources/application.yaml
  server.port: 8761
  eureka.client.registerWithEureka: false
  eureka.client.fetchRegistry: false
```

## Error Handling Strategy

### 1. Exception Detection

The `GlobalExceptionHandler` intercepts all exceptions thrown by the gateway:

```java
@Override
public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex)
```

### 2. Exception Classification

```
Exception Type                  HTTP Status    Error Message
─────────────────────────────────────────────────────────────
NotFoundException               503 or 404     Depends on details
  ├─ "Unable to find instance" → 503          Service Unavailable
  └─ Other cases               → 404          Not Found

IllegalArgumentException        400            Bad Request

All other Throwable            500            Internal Server Error
```

### 3. Response Building

```json
{
  "error": "error_type",
  "message": "human_readable_message",
  "status": http_status_code,
  "timestamp": "ISO_8601_timestamp",
  "path": "request_path"
}
```

### 4. Logging

Each error is logged with:
- HTTP Status Code
- Error Type
- Error Message
- Exception Details (if available)

Example: `[GATEWAY ERROR] Status: 503 Service Unavailable | Error: Service Unavailable | Message: ...`

## Service Startup Sequence

For the system to work correctly, services should be started in this order:

```
1. Eureka Server (8761)
   └─ Provides service registry

2. Auth Service (8081)
   └─ Registers with Eureka

3. User Service (8082)
   └─ Registers with Eureka

4. Startup Service (8083)
   └─ Registers with Eureka

5. API Gateway (8080)
   └─ Queries Eureka to discover services
   └─ Routes requests to appropriate services
```

## Eureka Service Registry

When services register with Eureka, they send a heartbeat every 30 seconds. If a heartbeat is not received for 90 seconds, the service is deregistered.

```
Service Lifecycle:
  Running + Heartbeat → Status: UP ✓
  No Heartbeat → Deregistration (after 90s) → Status: DOWN ✗
  Restart + Heartbeat → Status: UP ✓
```

## Testing the System

### All Services Up (Expected Behavior)

```bash
# Check Eureka
curl http://localhost:8761/eureka/apps
# Should show:
# - AUTHSERVICE (UP)
# - USERSERVICE (UP)
# - STARTUPSERVICE (UP)
# - APIGATEWAY (UP)

# Make a request
curl http://localhost:8080/founderlink/startups

# Expected: Response from StartupService (200, 401, 403, etc.)
```

### StartupService Down (Service Unavailability)

```bash
# Stop StartupService

# Check Eureka
curl http://localhost:8761/eureka/apps
# Should show:
# - AUTHSERVICE (UP)
# - USERSERVICE (UP)
# - STARTUPSERVICE (DOWN) or missing
# - APIGATEWAY (UP)

# Make a request
curl http://localhost:8080/founderlink/startups

# Expected: 503 Service Unavailable with error response
```

## Key Files

```
FounderLink/
├── apiGateway/apiGateway/
│   ├── src/main/
│   │   ├── java/com/founderlink/apiGateway/
│   │   │   ├── ApiGatewayApplication.java (Route configuration)
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java (NEW - Error handling)
│   │   │   ├── filter/
│   │   │   └── util/
│   │   └── resources/
│   │       └── application.yaml (Gateway configuration)
│   └── src/test/
│       └── java/com/founderlink/apiGateway/exception/
│           └── GlobalExceptionHandlerTest.java (NEW - Tests)
│
├── eurekaServer/
│   └── src/main/resources/
│       └── application.yaml (Eureka configuration)
│
├── authService/authService/
│   └── src/main/resources/
│       └── application.yaml (Auth service configuration)
│
├── userService/userService/
│   └── src/main/resources/
│       └── application.yaml (User service configuration)
│
├── startupService/startupService/
│   ├── src/main/
│   │   ├── java/com/founderlink/startupService/
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java (Service-level error handling)
│   │   └── resources/
│   │       └── application.yaml (Startup service configuration)
│   └── src/test/
│
├── ERROR_HANDLING_GUIDE.md (NEW - Comprehensive guide)
└── API_ERROR_REFERENCE.md (NEW - Quick reference)
```

## Monitoring & Debugging

### Health Endpoints

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Eureka health  
curl http://localhost:8761/actuator/health

# Service discovery info
curl http://localhost:8080/actuator/env | grep eureka

# Gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

### Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| 503 Service Unavailable | Service not running | Start the service |
| 503 Service Unavailable | Eureka unavailable | Start Eureka Server |
| 404 Not Found | Wrong endpoint path | Verify route path |
| 400 Bad Request | Invalid request body | Check JSON format |
| 500 Internal Server Error | Service error | Check service logs |

## Next Steps

1. ✅ GlobalExceptionHandler implementation - **COMPLETED**
2. ✅ Unit tests for exception handler - **COMPLETED**
3. ✅ Documentation - **COMPLETED**
4. 🔄 **Deploy and test** - Start all services and verify error handling
5. 🔄 **Monitor** - Watch logs for errors and patterns
6. 🔄 **Enhance** - Add more specific exception handlers as needed

