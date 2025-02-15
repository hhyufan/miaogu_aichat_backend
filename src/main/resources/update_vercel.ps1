# 加载环境变量模块
Write-Host "Current Directory: $(Get-Location)"
$scriptDir = $PSScriptRoot
$modulePath = Join-Path $scriptDir "EnvVariables.psm1"
Write-Host "Importing module from: $modulePath"
Import-Module $modulePath -Force

# Start ngrok and wait
$ngrokPath = "C:\ngrok\ngrok.exe"  # Replace with the actual path
Start-Process -FilePath $ngrokPath -ArgumentList "http 8088"
Start-Sleep -Seconds 5

# Retry to get API information
$maxRetries = 10
$retryCount = 0
$response = $null
do {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels" -ErrorAction Stop
        if (-not $response.tunnels) { throw "No valid tunnel information" }
    } catch {
        Write-Host "Error: $_ (Attempt $retryCount/$maxRetries)"
        $retryCount++
        Start-Sleep -Seconds 2
        if ($retryCount -ge $maxRetries) {
            throw "Unable to retrieve ngrok tunnel information. Please check if ngrok is running properly."
        }
    }
} until ($response -ne $null)

# Extract URL
$PUBLIC_URL = ($response.tunnels | Where-Object { $_.proto -eq 'https' })[0].public_url

# Update Vercel Edge Config
$uri = "https://api.vercel.com/v1/edge-config/$env:EDGE_CONFIG_ID/items"  # Use the Edge Config ID here
$headers = @{
    "Authorization" = "Bearer $env:VERCEL_ACCESS_TOKEN"  # Use the Vercel Access  token here
    "Content-Type" = "application/json"
}

$body = @{
    items = @(
        @{
            operation = "upsert"
            key = "API_URL"
            value = $PUBLIC_URL
        }
    )
} | ConvertTo-Json

# Debugging output
Write-Host "Request URI: $uri"
Write-Host "Request Body: $
';body"

# Attempt to update Vercel Edge Config
try {
    Invoke-RestMethod -Uri $uri -Method Patch -Headers $headers -Body $body
    Write-Host "Updated Vercel Edge Config with new URL: $PUBLIC_URL"
} catch {
    Write-Host "Failed to update Vercel Edge Config: $_"
}
