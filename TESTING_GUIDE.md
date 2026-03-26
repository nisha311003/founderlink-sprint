# Testing Guide - API Gateway Error Handling

## Quick Start Testing

### Prerequisites

Ensure all services can be started:

```powershell
# Terminal 1: Start Eureka Server
cd D:\IdeaProjects\FounderLink\eurekaServer
mvn spring-boot:run

# Terminal 2: Start Auth Service
cd D:\IdeaProjects\FounderLink\authService\authService
mvn spring-boot:run

# Terminal 3: Start User Service
cd D:\IdeaProjects\FounderLink\userService\userService
mvn spring-boot:run

# Terminal 4: Start Startup Service
cd D:\IdeaProjects\FounderLink\startupService\startupService
mvn spring-boot:run

# Terminal 5: Start API Gateway
cd D:\IdeaProjects\FounderLink\apiGateway\apiGateway
mvn spring-boot:run
```

### Health Check

```powershell
# Check all services are running
$services = @(8080, 8081, 8082, 8083, 8761)
foreach ($port in $services) {
    $response = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing -ErrorAction SilentlyContinue
    if ($response) {
        Write-Host "✓ Service on port $port is UP"
    } else {
        Write-Host "✗ Service on port $port is DOWN"
    }
}
```

## Test Scenarios

### Test 1: All Services Running (Normal Operation)

**Test Condition:** All services are running and registered with Eureka

**Commands:**
```powershell
# Check Eureka
$eureka = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -UseBasicParsing
$eureka.Content | Select-String "status>UP"

# Make API request
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "test@example.com"
    "Authorization" = "Bearer YOUR_TOKEN"  # If authentication required
}

$body = @{
    name = "Test Startup"
    description = "A test startup"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
    -Method POST `
    -Headers $headers `
    -Body $body `
    -UseBasicParsing -ErrorAction Continue

Write-Host "Status Code: $($response.StatusCode)"
Write-Host "Response: $($response.Content)"
```

**Expected Result:**
- HTTP 200-403 (depending on authentication)
- Service responds normally
- No error from GlobalExceptionHandler

---

### Test 2: StartupService Down (Service Unavailability)

**Test Condition:** StartupService is stopped/not running

**Setup:**
```powershell
# Stop StartupService (close the terminal or kill the process)
# Keep other services running
```

**Command:**
```powershell
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "test@example.com"
}

$body = @{
    name = "Test Startup"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -UseBasicParsing
} catch {
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.Value)"
    Write-Host "Response: $($_.Exception.Response | ConvertTo-Json -Depth 10)"
}
```

**Expected Result:**
```json
{
  "error": "Service Unavailable",
  "message": "The requested microservice is currently unavailable. Please try again later.",
  "status": 503,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

**Verify:**
- Status Code: **503**
- Header: `Content-Type: application/json`
- Message: User-friendly, not a stack trace
- No raw error details exposed

---

### Test 3: Eureka Server Down (All Services Unavailable)

**Test Condition:** Eureka Server is stopped

**Setup:**
```powershell
# Stop Eureka Server (close terminal or kill process)
# After ~90 seconds, services will be deregistered from gateway cache
```

**Command:**
```powershell
# Try to access any endpoint
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "test@example.com"
}

$body = @{ name = "Test" } | ConvertTo-Json

$response = try {
    Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -UseBasicParsing
} catch {
    $_.Exception.Response
}

Write-Host "Status: $($response.StatusCode)"
```

**Expected Result:**
- Status Code: **503**
- All endpoints return service unavailable
- Gateway cannot reach any services

**Recovery:**
```powershell
# Restart Eureka Server
cd D:\IdeaProjects\FounderLink\eurekaServer
mvn spring-boot:run

# Wait for services to re-register (~30-60 seconds)
# Then test again
```

---

### Test 4: Invalid Endpoint (404)

**Test Condition:** Request to non-existent endpoint

**Command:**
```powershell
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/founderlink/invalid/endpoint" `
        -UseBasicParsing
} catch {
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.Value)"
    
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $content = $reader.ReadToEnd()
    $reader.Close()
    
    $content | ConvertFrom-Json | ConvertTo-Json -Depth 10
}
```

**Expected Result:**
```json
{
  "error": "Not Found",
  "message": "The requested resource was not found",
  "status": 404,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/invalid/endpoint"
}
```

---

### Test 5: Invalid Request Body (400)

**Test Condition:** Send malformed JSON

**Command:**
```powershell
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "test@example.com"
}

$invalidJson = "{ invalid json }"

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $invalidJson `
        -UseBasicParsing
} catch {
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.Value)"
    
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $content = $reader.ReadToEnd()
    $reader.Close()
    
    Write-Host "Response: $content"
}
```

**Expected Result:**
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "status": 400,
  "timestamp": "2026-03-25T12:07:28",
  "path": "/founderlink/startups"
}
```

---

### Test 6: Missing Required Headers

**Test Condition:** Missing X-User-Id or X-User-Email headers

**Command:**
```powershell
# Missing X-User-Id header
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Email" = "test@example.com"
}

$body = @{
    name = "Test Startup"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -UseBasicParsing
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "This should be handled by the microservice, not the gateway"
}
```

**Expected Result:**
- If StartupService is running: Service returns appropriate error
- If StartupService is down: Gateway returns 503 Service Unavailable

---

## Advanced Testing

### Performance Testing

Test how the exception handler performs under load:

```powershell
# Simulate 100 requests to unavailable service
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

for ($i = 1; $i -le 100; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
            -Method POST `
            -Headers @{
                "Content-Type" = "application/json"
                "X-User-Id" = "$i"
                "X-User-Email" = "user$i@test.com"
            } `
            -Body (@{name = "Startup $i"} | ConvertTo-Json) `
            -UseBasicParsing -TimeoutSec 2 | Out-Null
    } catch {
        # Expected: 503 errors
    }
    
    Write-Host "Request $i completed"
}

$stopwatch.Stop()
Write-Host "Total time: $($stopwatch.ElapsedMilliseconds)ms for 100 requests"
Write-Host "Average: $($stopwatch.ElapsedMilliseconds / 100)ms per request"
```

### Log Analysis

Monitor the logs while testing:

```powershell
# View real-time logs (if using PowerShell console output)
Get-Content -Path "apiGateway.log" -Wait -Tail 10

# Or check console output in the terminal where gateway is running
# Look for lines starting with: [GATEWAY ERROR]
```

---

## Monitoring Logs

### What to Look For

**Success Log:**
```
✓ Application started
✓ Registered with Eureka
✓ Routes configured
✓ Exception handler initialized
```

**Error Logs (when testing service unavailability):**
```
[GATEWAY ERROR] Status: 503 Service Unavailable | 
Error: Service Unavailable | 
Message: The requested microservice is currently unavailable. Please try again later. | 
Details: 503 SERVICE_UNAVAILABLE "Unable to find instance for STARTUPSERVICE"
```

**Eureka Registration Logs:**
```
Registering application APIGATEWAY with Eureka server at [http://localhost:8761]
InstanceInfoInstanceStatus: STARTING → UP (after 30 seconds)
```

---

## Test Results Checklist

### When All Services Are Running
- [x] GET /founderlink/users returns 200-403
- [x] POST /founderlink/auth returns appropriate response
- [x] POST /founderlink/startups returns 200 or 401
- [x] Invalid endpoints return 404
- [x] Invalid JSON returns 400
- [x] All responses are JSON format
- [x] No stack traces in responses

### When StartupService is Down
- [x] All startup endpoints return 503
- [x] Response has "Service Unavailable" message
- [x] Status code is 503
- [x] Response includes timestamp and path
- [x] Content-Type is application/json
- [x] Error message is user-friendly
- [x] No technical details exposed

### When Eureka is Down
- [x] Gateway cannot discover services
- [x] All endpoints eventually return 503 (after cache expires)
- [x] Error messages are consistent
- [x] Gateway itself is still responsive

### When Gateway Receives Invalid Input
- [x] Malformed JSON returns 400
- [x] Invalid parameters return 400
- [x] Non-existent endpoints return 404
- [x] All error responses follow standard format

---

## Troubleshooting Failed Tests

### Issue: Still seeing stack traces in responses

**Solution:**
1. Verify GlobalExceptionHandler is in the correct package
2. Rebuild API Gateway: `mvn clean package`
3. Clear target directory: `rm -r target`
4. Restart gateway

### Issue: All requests return 500 errors

**Solution:**
1. Check if Eureka is running
2. Check if databases are accessible
3. Check service logs for startup errors
4. Verify network connectivity

### Issue: Tests pass but errors are still logging stack traces

**Solution:**
1. Check log level configuration
2. Ensure no other exception handlers are interfering
3. Verify GlobalExceptionHandler bean is being created
4. Add debug logging to exception handler

### Issue: Services take too long to register

**Normal behavior:**
- Service startup: 2-5 seconds
- Eureka registration: ~30 seconds
- Gateway discovery: ~30 seconds

If longer, check:
- Network connectivity
- Eureka server performance
- Service initialization issues

---

## Batch Testing Script

Save as `test_all.ps1`:

```powershell
function Test-Gateway {
    param(
        [string]$testName,
        [string]$url,
        [string]$method = "GET",
        [hashtable]$headers = @{},
        [string]$body = $null,
        [int]$expectedStatus = 200
    )
    
    Write-Host "`n=== Testing: $testName ===" -ForegroundColor Cyan
    
    try {
        if ($body) {
            $response = Invoke-WebRequest -Uri $url -Method $method -Headers $headers -Body $body -UseBasicParsing
        } else {
            $response = Invoke-WebRequest -Uri $url -Method $method -Headers $headers -UseBasicParsing
        }
        
        if ($response.StatusCode -eq $expectedStatus) {
            Write-Host "✓ PASS - Status: $($response.StatusCode)" -ForegroundColor Green
        } else {
            Write-Host "✗ FAIL - Expected $expectedStatus, got $($response.StatusCode)" -ForegroundColor Red
        }
        
        Write-Host "Response: $($response.Content | ConvertFrom-Json | ConvertTo-Json -Compress)"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()
        
        if ($statusCode -eq $expectedStatus) {
            Write-Host "✓ PASS - Status: $statusCode" -ForegroundColor Green
        } else {
            Write-Host "✗ FAIL - Expected $expectedStatus, got $statusCode" -ForegroundColor Red
        }
        
        Write-Host "Response: $content"
    }
}

# Run tests
Test-Gateway -testName "Health Check" -url "http://localhost:8080/actuator/health"
Test-Gateway -testName "Invalid Endpoint" -url "http://localhost:8080/founderlink/invalid" -expectedStatus 404
Test-Gateway -testName "Post Startup" -url "http://localhost:8080/founderlink/startups" `
    -method "POST" `
    -headers @{"Content-Type"="application/json"; "X-User-Id"="1"; "X-User-Email"="test@test.com"} `
    -body '{"name":"Test"}' `
    -expectedStatus 503

Write-Host "`n=== All Tests Complete ===" -ForegroundColor Cyan
```

Run with: `.\test_all.ps1`

---

**Happy Testing!** 🚀

