
$ADB = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"

$JAVA = "C:\Program Files\Java\jdk-14\bin\java.exe"

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

function Set-SDK {

    param ([String]$Path)

    $Path = $Path.Replace('\', '\\')

    "sdk.dir = $Path" ` | Set-Content "$PSScriptRoot\local.properties"

}

Export-ModuleMember `
    -Function * `
    -Variable *
