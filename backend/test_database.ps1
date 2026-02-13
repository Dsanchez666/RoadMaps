# ============================================================
# Test Script for MySQL and Oracle Database Connections
# PowerShell Version
# ============================================================

# Configuration
$BaseURL = "http://localhost:8080"
$Timeout = 5

function Write-Success {
    param([string]$Message)
    Write-Host "[✓ PASS] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[✗ FAIL] $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "[ℹ INFO] $Message" -ForegroundColor Cyan
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[⚠ WARN] $Message" -ForegroundColor Yellow
}

function Test-Endpoint {
    param(
        [string]$Uri,
        [string]$Method = "Get"
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Uri -Method $Method -ErrorAction Stop -TimeoutSec $Timeout
        return $response
    }
    catch {
        return $null
    }
}

# ============================================================
# Main Tests
# ============================================================

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Database Connection Test Script" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check backend connectivity
Write-Info "Checking backend connectivity..."
$statusResponse = Test-Endpoint "$BaseURL/api/database/supported-types"

if ($statusResponse) {
    Write-Success "Backend is running at $BaseURL"
}
else {
    Write-Error "Cannot reach backend at $BaseURL"
    Write-Host ""
    Write-Host "Please ensure backend is running:" -ForegroundColor Yellow
    Write-Host "  cd roadmap-mvp-project\backend"
    Write-Host "  mvn spring-boot:run" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host ""

# Test 2: Get supported database types
Write-Host "=== TEST 1: Supported Database Types ===" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "$BaseURL/api/database/supported-types" -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Response:" -ForegroundColor Gray
    $data | ConvertTo-Json -Depth 10 | Write-Host
    
    Write-Success "Retrieved supported database types"
}
catch {
    Write-Error "Failed to get supported types: $_"
}

Write-Host ""

# Test 3: Check current connection status
Write-Host "=== TEST 2: Current Connection Status ===" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "$BaseURL/api/database/status" -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Response:" -ForegroundColor Gray
    $data | ConvertTo-Json -Depth 5 | Write-Host
    
    if ($data.connected) {
        Write-Success "Currently connected to $($data.type)"
    }
    else {
        Write-Warn "No active database connection"
    }
}
catch {
    Write-Error "Failed to get status: $_"
}

Write-Host ""

# Test 4: Test MySQL Connection
Write-Host "=== TEST 3: MySQL Connection ===" -ForegroundColor Cyan
Write-Host ""

Write-Info "Testing MySQL connection with default parameters..."
Write-Host "Parameters:" -ForegroundColor Gray
Write-Host "  host: localhost"
Write-Host "  port: 3306"
Write-Host "  database: roadmap_mvp"
Write-Host "  user: root"
Write-Host "  password: (empty)"
Write-Host ""

try {
    $params = @{
        host = "localhost"
        port = "3306"
        database = "roadmap_mvp"
        user = "root"
        password = ""
    }
    
    $queryString = [System.Web.HttpUtility]::ParseQueryString([String]::Empty)
    foreach ($key in $params.Keys) {
        $queryString.Add($key, $params[$key])
    }
    
    $uri = "$BaseURL/api/database/connect/mysql?$($queryString.ToString())"
    $response = Invoke-WebRequest -Uri $uri -Method Post -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Response:" -ForegroundColor Gray
    $data | ConvertTo-Json -Depth 5 | Write-Host
    
    if ($data.status -eq "SUCCESS") {
        Write-Success "MySQL connection successful!"
        Write-Success "Connected to: $($data.connectionUrl)"
    }
    elseif ($data.status -eq "FAILED") {
        Write-Warn "MySQL driver not available or connection failed"
        Write-Host "Message: $($data.message)" -ForegroundColor Gray
    }
    else {
        Write-Error "Unexpected response status: $($data.status)"
    }
}
catch {
    Write-Error "MySQL connection test failed: $_"
}

Write-Host ""

# Test 5: Test Oracle Connection (with example parameters)
Write-Host "=== TEST 4: Oracle Connection (Example Parameters) ===" -ForegroundColor Cyan
Write-Host ""

Write-Warn "This test uses example Oracle parameters and will likely fail unless you have Oracle running."
Write-Host ""
Write-Info "To test with your Oracle database, modify the parameters below..."
Write-Host ""

Write-Host "Example parameters:" -ForegroundColor Gray
Write-Host "  host: oracle-server.example.com"
Write-Host "  port: 1521"
Write-Host "  sid: ORCL"
Write-Host "  user: system"
Write-Host "  password: oracle"
Write-Host ""

# Change these to your actual Oracle credentials
$oracleParams = @{
    host = "oracle-server.example.com"
    port = "1521"
    sid = "ORCL"
    user = "system"
    password = "oracle"
}

Write-Host "Attempting connection..." -ForegroundColor Gray

try {
    $queryString = [System.Web.HttpUtility]::ParseQueryString([String]::Empty)
    foreach ($key in $oracleParams.Keys) {
        $queryString.Add($key, $oracleParams[$key])
    }
    
    $uri = "$BaseURL/api/database/connect/oracle?$($queryString.ToString())"
    $response = Invoke-WebRequest -Uri $uri -Method Post -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Response:" -ForegroundColor Gray
    $data | ConvertTo-Json -Depth 5 | Write-Host
    
    if ($data.status -eq "SUCCESS") {
        Write-Success "Oracle connection successful!"
        Write-Success "Connected to: $($data.connectionUrl)"
    }
    else {
        Write-Warn "Oracle connection attempt completed with status: $($data.status)"
        Write-Host "Message: $($data.message)" -ForegroundColor Gray
    }
}
catch {
    Write-Warn "Oracle connection test encountered an error (expected with example parameters):"
    Write-Host "Error: $_" -ForegroundColor Gray
}

Write-Host ""

# Test 6: Final status
Write-Host "=== TEST 5: Final Status Check ===" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "$BaseURL/api/database/status" -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Current Status:" -ForegroundColor Gray
    $data | ConvertTo-Json -Depth 5 | Write-Host
}
catch {
    Write-Error "Failed to get final status: $_"
}

Write-Host ""

# Summary
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Success "Tests completed. Check output above for results."
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. If MySQL connected: Try creating a roadmap"
Write-Host "  2. If Oracle test failed:"
Write-Host "     - Update `$oracleParams in this script with your credentials"
Write-Host "     - Or configure via REST endpoints"
Write-Host "  3. To create a test roadmap:" -ForegroundColor Gray
Write-Host ""
Write-Host '     $body = @{title="Test"; description="Oracle Test"} | ConvertTo-Json' -ForegroundColor Gray
Write-Host '     Invoke-WebRequest -Uri "http://localhost:8080/api/roadmaps" -Method Post -Body $body -ContentType "application/json"' -ForegroundColor Gray
Write-Host ""
Write-Host "Additional Resources:" -ForegroundColor Yellow
Write-Host "  - DATABASE_CONFIGURATION.md"
Write-Host "  - ORACLE_INTEGRATION.md"
Write-Host "  - QUICKSTART.md"
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
