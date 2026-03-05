param(
    [ValidateSet("check", "backend", "backend-test", "frontend")]
    [string]$Action = "check"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $ProjectRoot "backend"
$FrontendDir = Join-Path $ProjectRoot "frontend"
$RequiredJdk = "C:\Program Files\Java\jdk-22"

function Use-BackendToolchain {
    if (-not (Test-Path $RequiredJdk)) {
        throw "JDK 22 no encontrado en '$RequiredJdk'."
    }
    $env:JAVA_HOME = $RequiredJdk
    $env:Path = "$RequiredJdk\bin;$env:Path"
}

function Show-Check {
    Write-Host "== CHECK TOOLCHAIN ==" -ForegroundColor Cyan
    Write-Host "Node por defecto:" -ForegroundColor Yellow
    node -v
    Write-Host "NPM por defecto:" -ForegroundColor Yellow
    npm -v
    Write-Host "Java por defecto en PATH:" -ForegroundColor Yellow
    java -version
    Write-Host "Javac por defecto en PATH:" -ForegroundColor Yellow
    javac -version
    Write-Host "Maven por defecto en PATH:" -ForegroundColor Yellow
    mvn -v
    Write-Host ""
    Write-Host "Diagnostico esperado para este proyecto:" -ForegroundColor Green
    Write-Host "- Frontend Angular 20: Node LTS instalado en PATH." -ForegroundColor Green
    Write-Host "- Backend Spring Boot 3.1.x: Java 17+ (forzado a JDK 22)." -ForegroundColor Green
}

function Start-Backend {
    Write-Host "== START BACKEND (SPRING BOOT 8080) ==" -ForegroundColor Cyan
    Use-BackendToolchain
    Set-Location $BackendDir
    mvn spring-boot:run
}

function Test-Backend {
    Write-Host "== TEST BACKEND ==" -ForegroundColor Cyan
    $resp = Invoke-WebRequest -Uri "http://localhost:8080/api/roadmaps" -UseBasicParsing -TimeoutSec 20
    Write-Host "GET /api/roadmaps -> $($resp.StatusCode)" -ForegroundColor Green
}

function Start-Frontend {
    Write-Host "== START FRONTEND (ANGULAR 4200) ==" -ForegroundColor Cyan
    Set-Location $FrontendDir
    $env:CI = "true"
    $env:NG_CLI_ANALYTICS = "false"
    node ./node_modules/@angular/cli/bin/ng.js serve --no-open --proxy-config proxy.conf.json
}

switch ($Action) {
    "check" { Show-Check }
    "backend" { Start-Backend }
    "backend-test" { Test-Backend }
    "frontend" { Start-Frontend }
}
