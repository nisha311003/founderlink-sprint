# API Gateway Error Response Reference

## HTTP Status Codes and Error Responses

| Status | Error | Meaning | Solution |
|--------|-------|---------|----------|
| **503** | Service Unavailable | Microservice is down or not registered with Eureka | Restart the service or check Eureka registration |
| **404** | Not Found | Endpoint doesn't exist or resource not found | Verify the endpoint path is correct |
| **400** | Bad Request | Invalid request parameters or malformed JSON | Check request body and parameters |
| **500** | Internal Server Error | Unexpected server-side error | Check service logs and contact support |

## Error Response Format

All error responses follow this JSON structure:

```json
{
  "error": "Error Type",
  "message": "Human-readable message",
  "status": 500,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/endpoint"
}
```

## Common Scenarios and Responses

### Scenario 1: StartupService is Down

**Request:**
```bash
curl -X POST http://localhost:8080/founderlink/startups \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-User-Email: user@example.com" \
  -d '{"name":"My Startup"}'
```

**Response (503):**
```json
{
  "error": "Service Unavailable",
  "message": "The requested microservice is currently unavailable. Please try again later.",
  "status": 503,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

**Fix:** Start the StartupService
```bash
cd startupService/startupService
mvn spring-boot:run
# OR
java -jar target/startupService-0.0.1-SNAPSHOT.jar
```

---

### Scenario 2: Invalid JSON in Request Body

**Request:**
```bash
curl -X POST http://localhost:8080/founderlink/auth/login \
  -H "Content-Type: application/json" \
  -d '{invalid json}'
```

**Response (400):**
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "status": 400,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/auth/login"
}
```

**Fix:** Ensure request body is valid JSON
```json
{"email": "user@example.com", "password": "password123"}
```

---

### Scenario 3: Endpoint Does Not Exist

**Request:**
```bash
curl http://localhost:8080/founderlink/invalid/endpoint
```

**Response (404):**
```json
{
  "error": "Not Found",
  "message": "The requested resource was not found",
  "status": 404,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/invalid/endpoint"
}
```

**Fix:** Use correct endpoint path:
- `/founderlink/auth/**` - Authentication endpoints
- `/founderlink/users/**` - User endpoints
- `/founderlink/startups/**` - Startup endpoints

---

### Scenario 4: Database Connection Error

**Request:**
```bash
curl -X POST http://localhost:8080/founderlink/startups \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-User-Email: user@example.com" \
  -d '{"name":"My Startup"}'
```

**Response (500):**
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact support if the problem persists.",
  "status": 500,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

**Fix:** 
1. Check the service logs: `docker logs startupService` or view console output
2. Verify database is running and accessible
3. Check database credentials in `application.yaml`

---

## Quick Diagnostics

### Check if All Services are Running

```bash
# Check Eureka for registered services
curl http://localhost:8761/eureka/apps

# Check individual service health
curl http://localhost:8080/actuator/health    # Gateway
curl http://localhost:8081/actuator/health    # AuthService
curl http://localhost:8082/actuator/health    # UserService
curl http://localhost:8083/actuator/health    # StartupService
curl http://localhost:8761/actuator/health    # EurekaServer
```

### View Service Logs

```powershell
# Windows: Find Java process
Get-Process java

# Then check if service is actually running on expected port
Test-NetConnection -ComputerName localhost -Port 8083
```

### Restart a Service

```bash
# Kill Java process (get PID from Get-Process java)
Stop-Process -Id <PID> -Force

# Start service
cd startupService/startupService
mvn spring-boot:run
```

## Implementation Notes

The GlobalExceptionHandler is configured as a Spring configuration bean that implements `ErrorWebExceptionHandler`. It:

1. **Catches all unhandled exceptions** from the gateway and route filters
2. **Analyzes exception type** to determine appropriate HTTP status code
3. **Generates consistent JSON responses** with ObjectMapper
4. **Sets proper Content-Type header** to `application/json`
5. **Logs all errors** with structured formatting
6. **Includes request path** for easier debugging

## Headers in Error Responses

All error responses include:
```
Content-Type: application/json
```

## For Frontend Developers

When handling API responses, check the status code:

```javascript
fetch('http://localhost:8080/founderlink/startups', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-User-Id': '1',
    'X-User-Email': 'user@example.com'
  },
  body: JSON.stringify({ name: 'My Startup' })
})
.then(response => {
  if (response.status === 503) {
    console.error('Service temporarily unavailable. Please try again later.');
    // Implement retry logic with exponential backoff
  } else if (response.status === 404) {
    console.error('Endpoint not found. Check the URL.');
  } else if (response.status === 400) {
    console.error('Invalid request. Check your parameters.');
  } else if (response.status === 500) {
    console.error('Server error. Please try again later.');
  }
  return response.json();
})
.catch(error => {
  console.error('Network error:', error);
});
```

