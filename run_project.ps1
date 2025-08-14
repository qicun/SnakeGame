# Run Project Script for Snake Game
# This script helps run the Snake Game project on Android and iOS platforms

# Set the correct JAVA_HOME - 自动检测JDK路径
$jdkPaths = @(
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot",
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk*",
    "C:\Program Files\Eclipse Adoptium\jdk*"
)

$jdkFound = $false
foreach ($path in $jdkPaths) {
    if ($path -like "*`*") {
        # 处理通配符路径
        $matchingPaths = Get-ChildItem -Path $path.Substring(0, $path.LastIndexOf("\")) -Directory | Where-Object { $_.Name -like $path.Substring($path.LastIndexOf("\")+1) }
        if ($matchingPaths.Count -gt 0) {
            $env:JAVA_HOME = $matchingPaths[0].FullName
            $jdkFound = $true
            break
        }
    } elseif (Test-Path $path) {
        $env:JAVA_HOME = $path
        $jdkFound = $true
        break
    }
}

if (-not $jdkFound) {
    Write-Host "无法找到有效的JDK路径。请手动设置JAVA_HOME环境变量。" -ForegroundColor Red
    Write-Host "您可以通过修改此脚本中的jdkPaths数组来添加其他可能的JDK路径。" -ForegroundColor Yellow
    exit 1
}

Write-Host "已设置JAVA_HOME为: $env:JAVA_HOME" -ForegroundColor Green

# Function to display menu
function Show-Menu {
    Clear-Host
    Write-Host "===== Snake Game Project Runner =====" -ForegroundColor Green
    Write-Host "1: Build Android Debug APK"
    Write-Host "2: Install Android Debug APK to connected device"
    Write-Host "3: Build iOS Framework"
    Write-Host "4: Open Xcode project"
    Write-Host "5: Clean project"
    Write-Host "6: Run tests"
    Write-Host "Q: Quit"
    Write-Host "===================================" -ForegroundColor Green
}

# Function to execute Gradle commands
function Execute-Gradle {
    param (
        [string]$command
    )
    
    Write-Host "Executing: ./gradlew $command" -ForegroundColor Cyan
    ./gradlew $command
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Command completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Command failed with exit code $LASTEXITCODE" -ForegroundColor Red
    }
    
    Write-Host "Press any key to continue..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Main loop
do {
    Show-Menu
    $input = Read-Host "Please make a selection"
    
    switch ($input) {
        '1' {
            Execute-Gradle "composeApp:assembleDebug"
            Write-Host "APK location: composeApp/build/outputs/apk/debug/composeApp-debug.apk" -ForegroundColor Yellow
        }
        '2' {
            Execute-Gradle "composeApp:installDebug"
        }
        '3' {
            Execute-Gradle "composeApp:linkDebugFrameworkIosX64"
            Write-Host "iOS Framework built successfully" -ForegroundColor Yellow
        }
        '4' {
            Write-Host "Opening Xcode project..." -ForegroundColor Cyan
            Start-Process "iosApp/iosApp.xcodeproj"
        }
        '5' {
            Execute-Gradle "clean"
        }
        '6' {
            Execute-Gradle "test"
        }
        'q' {
            return
        }
    }
} until ($input -eq 'q')