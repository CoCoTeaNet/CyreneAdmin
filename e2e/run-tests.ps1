# run-tests.ps1 - E2E Test Runner for Windows
param(
    [switch]$SkipBuild,
    [switch]$SkipInfra,
    [switch]$ApiOnly,
    [switch]$Headed,
    [switch]$CleanOnly,
    [int]$BuildParallel = 4
)

$ErrorActionPreference = "Stop"
$RootDir = (Get-Location).Path
$ProjectRoot = Split-Path $RootDir -Parent
$LogDir = Join-Path $RootDir "logs"
$BackendLog = Join-Path $LogDir "backend.log"
$FrontendLog = Join-Path $LogDir "frontend.log"
$RunLog = Join-Path $LogDir "run.log"
$BackendPidFile = Join-Path $LogDir "backend.pid"
$FrontendPidFile = Join-Path $LogDir "frontend.pid"
$StartScript = Join-Path $LogDir "start-backend.bat"
$BackendPort = 9000
$FrontendPort = 5173

function Log {
    param([string]$Level, [string]$Message)
    $ts = Get-Date -Format "HH:mm:ss"
    $line = "[$ts] [$Level] $Message"
    Write-Host $line
    Add-Content -Path $RunLog -Value $line -ErrorAction SilentlyContinue
}

function LogStep { Log "STEP" $args[0] }
function LogInfo { Log "INFO" $args[0] }
function LogWarn { Log "WARN" $args[0] }
function LogFail { Log "FAIL" $args[0] }

function Cleanup {
    LogInfo "=== Cleanup ==="
    if (Test-Path $BackendPidFile) {
        $stopId = (Get-Content $BackendPidFile).Trim()
        LogInfo "Stopping backend PID=$stopId"
        cmd /c "taskkill /PID $stopId /T /F >nul 2>&1"
        Remove-Item $BackendPidFile -ErrorAction SilentlyContinue
    }
    if (Test-Path $FrontendPidFile) {
        $stopId = (Get-Content $FrontendPidFile).Trim()
        LogInfo "Stopping frontend PID=$stopId"
        cmd /c "taskkill /PID $stopId /T /F >nul 2>&1"
        Remove-Item $FrontendPidFile -ErrorAction SilentlyContinue
    }
    foreach ($port in @($BackendPort, $FrontendPort)) {
        try {
            $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
            foreach ($c in $conns) {
                if ($c.OwningProcess -gt 4) {
                    cmd /c "taskkill /PID $($c.OwningProcess) /T /F >nul 2>&1"
                }
            }
        } catch {}
    }
    if (-not $SkipInfra) {
        LogInfo "Stopping infrastructure containers"
        Push-Location $RootDir
        cmd /c "docker compose -f docker-compose.test.yml down --remove-orphans >nul 2>&1"
        Pop-Location
    }
    LogInfo "Cleanup done"
}

function WaitForPort {
    param([int]$Port, [int]$TimeoutSec = 120, [string]$Label = "port")
    LogInfo "Waiting for $Label on port $Port (timeout ${TimeoutSec}s)"
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $TimeoutSec) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect("127.0.0.1", $Port)
            $tcp.Close()
            LogInfo "$Label is ready (${([int]$sw.Elapsed.TotalSeconds)}s)"
            return $true
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    LogFail "$Label did not start on port $Port within ${TimeoutSec}s"
    return $false
}

function IsProcessAlive {
    param([int]$ProcessId)
    if ($ProcessId -le 0) { return $false }
    try {
        $p = Get-Process -Id $ProcessId -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Init {
    LogStep "Initialize"
    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
    Set-Content -Path $RunLog -Value ""
    LogInfo "Log directory: $LogDir"
    LogInfo "Root directory: $RootDir"
}

function StartInfra {
    if ($SkipInfra) {
        LogInfo "Skipping infrastructure (skipInfra=true)"
        return
    }
    LogStep "Start MySQL and Redis"
    Push-Location $RootDir
    try {
        LogInfo "Starting containers..."
        cmd /c "docker compose -f docker-compose.test.yml up -d mysql redis 2>&1"
        $retries = 0
        while ($retries -lt 30) {
            $status = cmd /c "docker inspect --format={{.State.Health.Status}} e2e-mysql 2>nul"
            if ($status -eq "healthy") {
                LogInfo "MySQL is healthy"
                break
            }
            $retries++
            Start-Sleep -Seconds 2
        }
        if ($retries -ge 30) {
            LogFail "MySQL failed to become healthy"
            Pop-Location
            exit 1
        }
        $retries = 0
        while ($retries -lt 15) {
            $pong = cmd /c "docker exec e2e-redis redis-cli ping 2>nul"
            if ($pong -match "PONG") {
                LogInfo "Redis is ready"
                break
            }
            $retries++
            Start-Sleep -Seconds 1
        }
        LogInfo "Seeding captcha in Redis"
        cmd /c "docker exec e2e-redis redis-cli SET captcha:e2e-test-captcha e2e-test-captcha EX 300 2>nul"
        LogInfo "Infrastructure started"
    } finally {
        Pop-Location
    }
}

function Build {
    if ($SkipBuild) {
        LogInfo "Skipping build (skipBuild=true)"
        return
    }
    LogStep "Maven build"
    $ProjectRoot = Split-Path $RootDir -Parent
    Push-Location $ProjectRoot
    try {
        $env:MAVEN_OPTS = "-Xmx2g"
        LogInfo "mvn clean install -DskipTests -T $BuildParallel"
        mvn clean install -DskipTests -T $BuildParallel 2>&1 | Tee-Object -FilePath (Join-Path $LogDir "maven-build.log")
        if ($LASTEXITCODE -ne 0) {
            LogFail "Maven build failed"
            exit 1
        }
        LogInfo "Build succeeded"
    } finally {
        Pop-Location
    }
}

function StartBackend {
    LogStep "Start backend on port $BackendPort"
    $javaHome = $env:JAVA_HOME
    if (-not $javaHome) {
        $javaCmd = (Get-Command java -ErrorAction SilentlyContinue).Source
        if ($javaCmd) { $javaHome = Split-Path (Split-Path $javaCmd) }
    } else {
        $javaCmd = Join-Path $javaHome "bin\java.exe"
    }
    if (-not $javaCmd -or -not (Test-Path $javaCmd)) {
        LogFail "java not found. Set JAVA_HOME or add java to PATH."
        exit 1
    }
    $jar = Join-Path $ProjectRoot "cyrene-starter-solon\target\launcher.jar"
    if (-not (Test-Path $jar)) {
        LogFail "Backend JAR not found at $jar. Run build first."
        exit 1
    }
    $wrapperLines = @(
        "@echo off",
        "cd /d $RootDir",
        "set JAVA_OPTS=-Xmx512m -Dsolon.profiles.active=test --myapp.db1.password=test123456",
        "`"$javaCmd`" -jar `"$jar`"",
        "exit /b %ERRORLEVEL%"
    )
    Set-Content -Path $StartScript -Value ($wrapperLines -join "`r`n") -Encoding ASCII
    $proc = Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c", $StartScript `
        -WindowStyle Minimized `
        -RedirectStandardOutput $BackendLog `
        -RedirectStandardError (Join-Path $LogDir "backend-err.log") `
        -PassThru
    $proc.Id | Out-File -FilePath $BackendPidFile -Encoding ASCII -NoNewline
    LogInfo "Backend process started: PID=$($proc.Id)"
    Start-Sleep -Seconds 5
    if (-not (IsProcessAlive $proc.Id)) {
        LogFail "Backend process died immediately"
        if (Test-Path $BackendLog) {
            Get-Content $BackendLog -Tail 30 | ForEach-Object { LogFail "  $_" }
        }
        exit 1
    }
    LogInfo "Backend process is alive"
}

function StartFrontend {
    LogStep "Start frontend on port $FrontendPort"
    Push-Location (Join-Path $ProjectRoot "cyrene-ui")
    try {
        if (-not (Test-Path "node_modules")) {
            LogInfo "Installing frontend dependencies"
            cmd /c "npm install 2>&1"
        }
        $env:VITE_API_BASE = "http://localhost:$BackendPort"
        $env:VITE_CAPTCHA_BYPASS = "e2e-test-captcha"
        $devCmd = if ($Headed) { "npx vite --port $FrontendPort" } else { "npx vite --host 127.0.0.1 --port $FrontendPort" }
        $proc = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c", "cd /d `"$ProjectRoot\cyrene-ui`" && set VITE_API_BASE=http://localhost:$BackendPort && set VITE_CAPTCHA_BYPASS=e2e-test-captcha && $devCmd" `
            -WindowStyle Minimized `
            -RedirectStandardOutput $FrontendLog `
            -RedirectStandardError (Join-Path $LogDir "frontend-err.log") `
            -PassThru
        $proc.Id | Out-File -FilePath $FrontendPidFile -Encoding ASCII -NoNewline
        LogInfo "Frontend process started: PID=$($proc.Id)"
        Start-Sleep -Seconds 3
        if (-not (IsProcessAlive $proc.Id)) {
            LogFail "Frontend process died immediately"
            exit 1
        }
        LogInfo "Frontend process is alive"
    } finally {
        Pop-Location
    }
}

function WaitForBackend {
    if (-not (WaitForPort -Port $BackendPort -TimeoutSec 120 -Label "backend")) {
        LogFail "Backend failed to start"
        if (Test-Path $BackendLog) {
            Get-Content $BackendLog -Tail 60 | ForEach-Object { LogFail "  $_" }
        }
        exit 1
    }
    Start-Sleep -Seconds 3
    LogInfo "Backend API is reachable"
}

function WaitForFrontend {
    if (-not (WaitForPort -Port $FrontendPort -TimeoutSec 60 -Label "frontend")) {
        LogFail "Frontend failed to start"
        exit 1
    }
    LogInfo "Frontend dev server is reachable"
}

function SeedCaptcha {
    LogInfo "Seeding test captcha in Redis"
    cmd /c "docker exec e2e-redis redis-cli SET captcha:e2e-test-captcha e2e-test-captcha EX 600 2>nul"
}

function RunTests {
    LogStep "Run Playwright tests"
    Push-Location $RootDir
    try {
        if (-not (Test-Path "node_modules\@playwright\test")) {
            LogInfo "Installing e2e dependencies"
            cmd /c "cd /d `"$RootDir`" && npm install 2>&1"
        }
        $env:BASE_URL = "http://localhost:$FrontendPort"
        $env:API_BASE = "http://localhost:$BackendPort"
        $headlessArg = if ($Headed) { "--headed" } else { "" }
        $filterArg = if ($ApiOnly) { "tests/api/" } else { "tests/" }
        $cmd = "npx --yes playwright test $filterArg --workers=1 $headlessArg"
        LogInfo "Command: $cmd"
        Invoke-Expression $cmd
        if ($LASTEXITCODE -ne 0) {
            LogFail "Playwright tests failed (exit $LASTEXITCODE)"
            $reportSrc = Join-Path $RootDir "playwright-report\index.html"
            if (Test-Path $reportSrc) {
                Copy-Item $reportSrc (Join-Path $LogDir "report.html") -Force
                LogInfo "HTML report copied to $LogDir\report.html"
            }
            exit $LASTEXITCODE
        }
        LogInfo "All tests passed!"
    } finally {
        Pop-Location
    }
}

function Summary {
    param([string]$Result)
    LogInfo ""
    LogInfo "========== E2E Test Result: $Result =========="
    LogInfo ""
    LogInfo "Logs:"
    LogInfo "  Run:       $RunLog"
    LogInfo "  Backend:   $BackendLog"
    LogInfo "  Frontend:  $FrontendLog"
    $reportPath = Join-Path $LogDir "report.html"
    if (Test-Path $reportPath) {
        LogInfo "  Report:    $reportPath"
    }
    LogInfo ""
}

# ---- entry ----
if ($CleanOnly) {
    Cleanup
    LogInfo "Clean only completed"
    exit 0
}

Init
StartInfra
Build
StartBackend
WaitForBackend
SeedCaptcha
StartFrontend
WaitForFrontend

$testExit = 0
try {
    RunTests
    Summary "SUCCESS"
} catch {
    $testExit = 1
    Summary "FAILED"
} finally {
    Cleanup
}

exit $testExit
