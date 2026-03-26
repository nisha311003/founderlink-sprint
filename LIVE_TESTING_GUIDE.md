# ✅ UNIT TESTS PASSED - Endpoint Testing Guide

## 🎉 Test Results

**BUILD SUCCESS** ✅

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

### Tests Passed:
✅ testServiceNotFoundException() → Verifies 503 response  
✅ testGenericNotFoundException() → Verifies 404 response  
✅ testIllegalArgumentException() → Verifies 400 response  
✅ testGeneralException() → Verifies 500 response  

The GlobalExceptionHandler is **working perfectly**! You can see the [GATEWAY ERROR] logs showing all error types are being captured and logged properly.

---

## 🚀 Now Test Your Live Endpoint

To test your endpoint with a running application, follow these steps:

### Step 1: Start All Services

Open 5 separate PowerShell terminals and run these commands (in order):

**Terminal 1 - Start Eureka Server:**
```powershell
cd D:\IdeaProjects\FounderLink\eurekaServer
mvn spring-boot:run
```
*Wait for: "Eureka Server started" message (~30 seconds)*

**Terminal 2 - Start Auth Service:**
```powershell
cd D:\IdeaProjects\FounderLink\authService\authService
mvn spring-boot:run
```
*Wait for startup to complete (~20 seconds)*

**Terminal 3 - Start User Service:**
```powershell
cd D:\IdeaProjects\FounderLink\userService\userService
mvn spring-boot:run
```
*Wait for startup to complete (~20 seconds)*

**Terminal 4 - Start Startup Service:**
```powershell
cd D:\IdeaProjects\FounderLink\startupService\startupService
mvn spring-boot:run
```
*Wait for startup to complete (~20 seconds)*

**Terminal 5 - Start API Gateway:**
```powershell
cd D:\IdeaProjects\FounderLink\apiGateway\apiGateway
mvn spring-boot:run
```
*Wait for: "ApiGatewayApplication started" message (~15 seconds)*

---

### Step 2: Verify All Services Are Running

In a new PowerShell terminal, run:

```powershell
curl http://localhost:8761/eureka/apps | Select-String "status>UP" -Context 1
```

**Expected output:** Should see 4 services as UP:
- AUTHSERVICE
- USERSERVICE
- STARTUPSERVICE
- APIGATEWAY

---

### Step 3: Test Your Endpoint

You now have 4 test scenarios to try:

#### Test 1: Normal Request (All Services Running) ✅

```powershell
$body = @{
    name = "TechStartup"
    description = "An innovative tech company"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "founder@example.com"
    "Authorization" = "Bearer your_token_here"
}

Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
    -Method POST `
    -Headers $headers `
    -Body $body `
    -UseBasicParsing
```

**Expected Response:**
- **Status:** 200 OK (if authenticated) or 401/403 (if auth needed)
- **Body:** Actual startup data

---

#### Test 2: Service Unavailable (Stop StartupService) ❌➡️503

**Before running this test:**
1. Go to Terminal 4 (where StartupService is running)
2. Press `Ctrl+C` to stop it

**Then run:**
```powershell
$body = @{
    name = "TechStartup"
    description = "An innovative tech company"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "founder@example.com"
}

try {
    Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -UseBasicParsing
} catch {
    Write-Host "Status Code:" $_.Exception.Response.StatusCode.Value
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $content = $reader.ReadToEnd()
    $reader.Close()
    Write-Host "Response:" $content
}
```

**Expected Response (503):**
```json
{
  "error": "Service Unavailable",
  "message": "The requested microservice is currently unavailable. Please try again later.",
  "status": 503,
  "timestamp": "2026-03-25T12:23:45",
  "path": "/founderlink/startups"
}
```

✅ **This proves your GlobalExceptionHandler is working!**

---

#### Test 3: Invalid Endpoint (404)

```powershell
try {
    Invoke-WebRequest -Uri "http://localhost:8080/founderlink/invalid/path" `
        -UseBasicParsing
} catch {
    Write-Host "Status Code:" $_.Exception.Response.StatusCode.Value
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $content = $reader.ReadToEnd()
    $reader.Close()
    Write-Host "Response:" $content
}
```

**Expected Response (404):**
```json
{
  "error": "Not Found",
  "message": "The requested resource was not found",
  "status": 404,
  "timestamp": "2026-03-25T12:23:45",
  "path": "/founderlink/invalid/path"
}
```

---

#### Test 4: Invalid Request Body (400)

```powershell
$invalidJson = "{ invalid json }"

$headers = @{
    "Content-Type" = "application/json"
    "X-User-Id" = "1"
    "X-User-Email" = "founder@example.com"
}

try {
    Invoke-WebRequest -Uri "http://localhost:8080/founderlink/startups" `
        -Method POST `
        -Headers $headers `
        -Body $invalidJson `
        -UseBasicParsing
} catch {
    Write-Host "Status Code:" $_.Exception.Response.StatusCode.Value
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $content = $reader.ReadToEnd()
    $reader.Close()
    Write-Host "Response:" $content
}
```

**Expected Response (400):**
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "status": 400,
  "timestamp": "2026-03-25T12:23:45",
  "path": "/founderlink/startups"
}
```

---

## 📊 Test Checklist

### Unit Tests (Already Passed ✅)
- [x] testServiceNotFoundException() - ✅ PASSED
- [x] testGenericNotFoundException() - ✅ PASSED
- [x] testIllegalArgumentException() - ✅ PASSED
- [x] testGeneralException() - ✅ PASSED

### Integration Tests (Manual - Your Choice)
- [ ] Test with all services running (200 OK)
- [ ] Test with StartupService down (503 Service Unavailable)
- [ ] Test invalid endpoint (404 Not Found)
- [ ] Test invalid request body (400 Bad Request)

---

## 📝 What to Look For

In the **Gateway Terminal (Terminal 5)**, you should see logs like:

```
[GATEWAY ERROR] Status: 503 Service Unavailable | Error: Service Unavailable | Message: The requested microservice is currently unavailable. Please try again later. | Details: 503 SERVICE_UNAVAILABLE "Unable to find instance for STARTUPSERVICE"
```

This confirms the GlobalExceptionHandler is:
- ✅ Catching exceptions
- ✅ Logging errors properly
- ✅ Returning the right status codes
- ✅ Formatting responses as JSON

---

## 🎯 Quick Reference - Port Mapping

| Service | Port | Status |
|---------|------|--------|
| **Eureka Server** | 8761 | Terminal 1 - Check with: `curl http://localhost:8761/eureka/apps` |
| **Auth Service** | 8081 | Terminal 2 - Running |
| **User Service** | 8082 | Terminal 3 - Running |
| **Startup Service** | 8083 | Terminal 4 - Running (or stopped for test) |
| **API Gateway** | 8080 | Terminal 5 - Running (test endpoint here) |

---

## 🚨 Troubleshooting

### "Cannot connect to localhost:8080"
- ✅ Ensure Terminal 5 (Gateway) is running
- ✅ Check for port conflicts
- ✅ Wait ~30 seconds after starting gateway

### "Unable to find instance for STARTUPSERVICE"
- ✅ This is expected if you stopped the service (Test 2)
- ✅ Restart the service and wait 30 seconds for re-registration
- ✅ Check Eureka at `http://localhost:8761/eureka/apps`

### "Connection refused on port 8761"
- ✅ Start Eureka Server first (Terminal 1)
- ✅ Wait for it to fully start

### Tests show different status codes
- ✅ Check your request headers
- ✅ Verify the request path is correct
- ✅ Review the error message in the response

---

## 📚 Documentation Reference

For more details on:
- **Error types:** See `API_ERROR_REFERENCE.md`
- **Complete testing guide:** See `TESTING_GUIDE.md`
- **Architecture:** See `ARCHITECTURE_AND_ERROR_HANDLING.md`

---

## 🎉 Success Indicators

You'll know everything is working when:

1. ✅ Unit tests pass (already done!)
2. ✅ All 5 services start without errors
3. ✅ Eureka shows all 4 services as UP
4. ✅ API Gateway logs show [GATEWAY ERROR] entries
5. ✅ Responses are properly formatted JSON
6. ✅ Status codes match expected values

---

**Status: Ready for Testing! 🚀**

Start with Step 1 above to begin testing your endpoint.

