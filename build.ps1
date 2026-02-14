param(
    [Switch] $ClearCache
)

# CD to the root directory
Set-Location $PSScriptRoot

#==========================================================================

function Test-ADBConnection {

    $devices = adb.exe devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Clear-Cache {

    if (!$ClearCache) {
        return
    }

    Write-Host 'Clearing Cache ...'

    $Dirs = @(
        ".gradle",
        "build",
        "smarttubetv\build",
        "common\build"
    )

    foreach ($Dir in $Dirs) {

        Remove-Item `
            -Path $Dir `
            -Force `
            -Recurse `
            -ErrorAction SilentlyContinue `
            | Out-Null
    }

}

#==========================================================================
# GIT

git.exe submodule update --init

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

Clear-Cache

# Execute Gradle
& "$env:JAVA_HOME/bin/java.exe" `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug" `
    "--warning-mode" "all" `
    @args

Clear-Cache

#==========================================================================