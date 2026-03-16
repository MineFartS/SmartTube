
function Test-ADBConnection {

    $devices = adb.exe devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Connect-ADB {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        $IP = Read-Host 'Target IP Address'

        adb.exe connect $IP

        while (-not (Test-ADBConnection)) {

            Write-Host 'Awaiting Connection ...'

        }
        
    }

}

Export-ModuleMember -Function Connect-ADB