
$ANDROID_SDK = "$env:USERPROFILE\AppData\Local\Android\SDK"

$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JAVA = "C:\Program Files\Java\jdk-14\bin\java.exe"

$APP_ID = "minefarts.smarttube"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Connect-ADB {

    # Set Android SDK Path
    Repair-AndroidSDK

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        $IP = Read-Host 'Target IP Address'

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {

            Write-Host 'Awaiting Connection ...'

        }
        
    }

}

function Repair-AndroidSDK {

    $Path = $ANDROID_SDK.Replace('\', '\\') + '\\'

    "sdk.dir = $Path" ` | Set-Content "$PSScriptRoot\local.properties"

}

function Get-PID {
    return & $ADB shell pidof $APP_ID
}

Export-ModuleMember `
    -Function * `
    -Variable *
