# Starts the whole store locally, each piece in its own PowerShell window.
#   .\run-local.ps1            -> backend on PostgreSQL (needs `docker compose up -d` first)
#   .\run-local.ps1 -H2        -> backend on in-memory H2 (no Docker needed, data resets on restart)
param([switch]$H2)

$root = $PSScriptRoot
$profile = if ($H2) { "" } else { '"-Dspring-boot.run.profiles=postgres"' }   # quoted: PowerShell splits unquoted -Dfoo.bar=x at the dot
$ssl = '$env:JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"; '   # corporate-proxy fix, harmless otherwise

function Start-Window($title, $command) {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$Host.UI.RawUI.WindowTitle='$title'; cd '$root'; $ssl$command"
}

Start-Window "discovery (8761)" ".\mvnw.cmd -pl discovery-service spring-boot:run"
Start-Sleep 25                                     # Eureka must be up before the others register
Start-Window "product (8081)"   ".\mvnw.cmd -pl product-service spring-boot:run $profile"
Start-Sleep 10
Start-Window "order (8082)"     ".\mvnw.cmd -pl order-service spring-boot:run $profile"
Start-Window "gateway (8080)"   ".\mvnw.cmd -pl api-gateway spring-boot:run"
Start-Window "frontend (5173)"  "cd frontend; if (-not (Test-Path node_modules)) { npm install }; npm run dev"

Write-Host "Started. Open http://localhost:5173 (store), http://localhost:8761 (Eureka), http://localhost:5050 (pgAdmin)."
