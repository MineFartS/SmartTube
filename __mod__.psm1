
$ANDROID_SDK = "$PSScriptRoot\SDK"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JDK = "C:\Program Files\Java\jdk-17"
$JAVA = "$JDK\bin\java.exe"

$APP_ID = "minefarts.smarttube"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Invoke-Gradle {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

    & $JAVA `
        '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
        'org.gradle.wrapper.GradleWrapperMain' `
        @cmdargs

}

function Invoke-ADB {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        Write-Host "Enter Target IP Address or leave blank to skip install"
        $IP = Read-Host 'Target IP Address'

        if ($IP -eq "") {return $false}

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {
            Write-Host 'Awaiting Connection ...'
        }
        
    }

    if ($cmdargs.Count -gt 0) {
        & $ADB @cmdargs
    }

}

function Repair-Environment {

    Set-Location $PSScriptRoot

    #=======================================================
    # ANDROID_SDK

    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $ANDROID_SDK, "Machine")

    git.exe submodule update --init --recursive --remote
    
    if (-not (Test-Path "$ANDROID_SDK\.knownPackages")) {
        & "$ANDROID_SDK\Accept.ps1"
    }

    #=======================================================
    # JAVA_HOME

    Write-Output "org.gradle.java.home=$JDK" > 'local.properties'
    
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $JDK, "Machine")

    #=======================================================
    # Path

    $dirs = @(
        "$JDK\bin\",
        "$ANDROID_SDK\platform-tools\"
    )

    $Path = [Environment]::GetEnvironmentVariable("Path", "Machine")

    $dirs = ($dirs | Where-Object {$Path -notlike "*$_*"})

    foreach ($dir in $dirs) {
        $Path += ";$dir"
    }

    [Environment]::SetEnvironmentVariable("Path", $Path, "Machine")

}

Export-ModuleMember `
    -Function * `
    -Variable *
