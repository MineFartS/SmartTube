
$ANDROID_SDK = "$env:USERPROFILE\AppData\Local\Android\SDK"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JDK = "C:\Program Files\Java\latest\jdk-21"
$JAVA = "$JDK\bin\java.exe"

$APP_ID = "minefarts.smarttube"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Connect-ADB {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        $IP = Read-Host 'Target IP Address'

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {

            Write-Host 'Awaiting Connection ...'

        }
        
    }

}

function Get-PID {
    return & $ADB shell pidof $APP_ID
}

function Repair-Environment {

    Set-Location $PSScriptRoot

    #=======================================================

    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $ANDROID_SDK, "Machine")

    #=======================================================
    # JAVA_HOME
    
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $JDK, "Machine")

    #=======================================================
    # Path

    $Path = [Environment]::GetEnvironmentVariable("Path", "Machine")

    $javabin = ";$JDK\bin\"

    if ($Path -notcontains $javabin) {
        [Environment]::SetEnvironmentVariable("Path", $Path+$javabin, "Machine")
    }

}

Export-ModuleMember `
    -Function * `
    -Variable *
