param(
    [string]$BaseUrl = "http://localhost:8081",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [int]$Iterations = 5,
    [int]$TargetMs = 200
)

$ErrorActionPreference = "Stop"

function Invoke-TimedJsonRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $request = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $request.ContentType = "application/json"
        $request.Body = $Body | ConvertTo-Json -Depth 8
    }

    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-WebRequest @request
    $watch.Stop()

    [pscustomobject]@{
        Method = $Method
        Path = $Uri.Replace($BaseUrl, "")
        Status = $response.StatusCode
        ElapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
        Content = $response.Content
    }
}

Write-Host "Checking API performance at $BaseUrl"

$login = Invoke-TimedJsonRequest -Method "POST" -Uri "$BaseUrl/api/auth/login" -Body @{
    username = $Username
    password = $Password
}
$token = ($login.Content | ConvertFrom-Json).token

if ([string]::IsNullOrWhiteSpace($token)) {
    throw "Login succeeded but no JWT token was returned."
}

$headers = @{
    Authorization = "Bearer $token"
}

$endpoints = @(
    "/actuator/health",
    "/api/warehouses",
    "/api/products",
    "/api/inventory/snapshots",
    "/api/orders",
    "/api/users"
)

$results = New-Object System.Collections.Generic.List[object]
$results.Add($login)

for ($i = 1; $i -le $Iterations; $i++) {
    foreach ($endpoint in $endpoints) {
        $headersForRequest = $headers
        if ($endpoint -eq "/actuator/health") {
            $headersForRequest = @{}
        }

        $results.Add((Invoke-TimedJsonRequest -Method "GET" -Uri "$BaseUrl$endpoint" -Headers $headersForRequest))
    }
}

$summary = $results |
    Select-Object Method, Path, Status, ElapsedMs |
    Sort-Object ElapsedMs -Descending

$average = [math]::Round(($results | Measure-Object -Property ElapsedMs -Average).Average, 2)
$maximum = [math]::Round(($results | Measure-Object -Property ElapsedMs -Maximum).Maximum, 2)
$status = if ($maximum -le $TargetMs) { "PASS" } else { "REVIEW" }

$summary | Format-Table -AutoSize
Write-Host ""
Write-Host "Average: $average ms"
Write-Host "Maximum: $maximum ms"
Write-Host "Target:  <= $TargetMs ms"
Write-Host "Result:  $status"
