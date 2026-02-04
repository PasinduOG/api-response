# Build script to generate Javadoc and check for warnings
# This script will run Maven to generate Javadoc for the api-response project

Write-Host "Building Javadoc for api-response project..." -ForegroundColor Cyan
Write-Host ""

# Check if Maven is available
$mavenCmd = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $mavenCmd) {
    Write-Host "Maven not found in PATH. Searching for Maven installation..." -ForegroundColor Yellow

    # Common Maven installation paths
    $possiblePaths = @(
        "$env:MAVEN_HOME\bin\mvn.cmd",
        "C:\Program Files\Apache\Maven\bin\mvn.cmd",
        "C:\Program Files\Maven\bin\mvn.cmd",
        "$env:ProgramFiles\Apache\Maven\bin\mvn.cmd",
        "$env:USERPROFILE\apache-maven\bin\mvn.cmd"
    )

    $mavenPath = $null
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $mavenPath = $path
            Write-Host "Found Maven at: $mavenPath" -ForegroundColor Green
            break
        }
    }

    if (-not $mavenPath) {
        Write-Host "ERROR: Maven (mvn) not found. Please install Maven or add it to PATH." -ForegroundColor Red
        Write-Host "Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
        exit 1
    }

    $mvnCommand = $mavenPath
} else {
    $mvnCommand = "mvn"
    Write-Host "Using Maven from PATH: $($mavenCmd.Source)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Running: $mvnCommand clean javadoc:jar" -ForegroundColor Cyan
Write-Host ""

# Run Maven javadoc generation
& $mvnCommand clean javadoc:jar

Write-Host ""
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Javadoc generation completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Generated files:" -ForegroundColor Cyan
    Write-Host "  - target/api-response-2.0.0-javadoc.jar" -ForegroundColor White
    Write-Host "  - target/apidocs/ (HTML documentation)" -ForegroundColor White
} else {
    Write-Host "✗ Javadoc generation failed with exit code: $LASTEXITCODE" -ForegroundColor Red
}
