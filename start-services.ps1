# Function to check if PostgreSQL is running
function Test-PostgreSQLConnection {
    try {
        $conn = New-Object System.Data.Odbc.OdbcConnection
        $conn.ConnectionString = "Driver={PostgreSQL UNICODE};Server=localhost;Port=5432;Database=postgres;Uid=postgres;Pwd=Nut@n803212;"
        $conn.Open()
        $conn.Close()
        return $true
    }
    catch {
        Write-Host "PostgreSQL connection failed: $_"
        return $false
    }
}

# Function to create database if it doesn't exist
function Create-DatabaseIfNotExists {
    param (
        [string]$DatabaseName
    )
    try {
        $conn = New-Object System.Data.Odbc.OdbcConnection
        $conn.ConnectionString = "Driver={PostgreSQL UNICODE};Server=localhost;Port=5432;Database=postgres;Uid=postgres;Pwd=Nut@n803212;"
        $conn.Open()
        
        $cmd = $conn.CreateCommand()
        $cmd.CommandText = "SELECT 1 FROM pg_database WHERE datname = '$DatabaseName'"
        $result = $cmd.ExecuteScalar()
        
        if ($result -eq $null) {
            Write-Host "Creating database $DatabaseName..."
            $cmd.CommandText = "CREATE DATABASE $DatabaseName"
            $cmd.ExecuteNonQuery()
            Write-Host "Database $DatabaseName created successfully"
        }
        else {
            Write-Host "Database $DatabaseName already exists"
        }
        
        $conn.Close()
    }
    catch {
        Write-Host "Error creating database $DatabaseName : $_"
    }
}

# Function to wait for a service to be ready
function Wait-ForService {
    param (
        [string]$ServiceName,
        [string]$Url,
        [int]$MaxAttempts = 30,
        [int]$DelaySeconds = 5
    )
    
    $attempts = 0
    while ($attempts -lt $MaxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method GET -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Host "$ServiceName is ready!"
                return $true
            }
        }
        catch {
            Write-Host "Waiting for $ServiceName to be ready... (Attempt $($attempts + 1)/$MaxAttempts)"
            Start-Sleep -Seconds $DelaySeconds
            $attempts++
        }
    }
    Write-Host "$ServiceName failed to start after $MaxAttempts attempts"
    return $false
}

# Check if PostgreSQL is running
if (-not (Test-PostgreSQLConnection)) {
    Write-Host "PostgreSQL is not running. Please start PostgreSQL and try again."
    exit 1
}

# Create required databases
$databases = @(
    "demoapp_menu",
    "demoapp_user",
    "demoapp_payment",
    "demoapp_owner",
    "demoapp_delivery",
    "demoapp_order",
    "demoapp_auth",
    "demoapp_otp",
    "demoapp_admin",
    "demoapp_subscription",
    "demoapp_mess"
)

foreach ($db in $databases) {
    Create-DatabaseIfNotExists -DatabaseName $db
}

# Start Config Server
Write-Host "Starting Config Server..."
Start-Process -FilePath "java" -ArgumentList "-jar", "config-server/target/config-server-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 30

# Start Discovery Server
Write-Host "Starting Discovery Server..."
Start-Process -FilePath "java" -ArgumentList "-jar", "discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 30

# Start Authentication Service
Write-Host "Starting Authentication Service..."
Start-Process -FilePath "java" -ArgumentList "-jar", "auth-service/target/auth-service-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 45

# Start User Service
Write-Host "Starting User Service..."
Start-Process -FilePath "java" -ArgumentList "-jar", "user-service/target/user-service-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 45

# Start Menu Service
Write-Host "Starting Menu Service..."
Start-Process -FilePath "java" -ArgumentList "-jar", "menu-module/target/menu-module-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 45

# Start Order Service
Write-Host "Starting Order Service..."
Start-Process -FilePath "java" -ArgumentList "-jar", "order-service/target/order-service-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 45

# Start Delivery Service
Write-Host "Starting Delivery Service..."
Start-Process -FilePath "java" -ArgumentList "-jar", "delivery-service/target/delivery-service-0.0.1-SNAPSHOT.jar" -NoNewWindow
Start-Sleep -Seconds 45

# Start API Gateway
Write-Host "Starting API Gateway..."
Start-Process -FilePath "java" -ArgumentList "-jar", "api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar" -NoNewWindow

Write-Host "All services started. Ports:"
Write-Host "Config Server: 8888"
Write-Host "Discovery Server: 8761"
Write-Host "Authentication Service: 8081"
Write-Host "User Service: 8082"
Write-Host "Menu Service: 8083"
Write-Host "Order Service: 8085"
Write-Host "Delivery Service: 8086"
Write-Host "API Gateway: 8080" 