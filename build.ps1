param(
    [Switch] $ClearCache
)

#==========================================================================

function Test-ADBConnection {

    $devices = adb.exe devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

#==========================================================================
# INIT

# Clear the Terminal
Clear-Host

# CD to the root directory
Set-Location $PSScriptRoot

#==========================================================================
# ADB

if (-not (Test-ADBConnection)) {

    Write-Host "No ADB device is connected"
    $IP = Read-Host 'Target IP Address'

    adb.exe connect $IP

    while (-not (Test-ADBConnection)) {

        Write-Host 'Awaiting Connection ...'

    }
    
}

#==========================================================================
# GRADLE

# Execute Gradle
& "$env:JAVA_HOME/bin/java.exe" `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug" `
    "--warning-mode" "all"

#==========================================================================