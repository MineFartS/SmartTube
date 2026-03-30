Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Connect-ADB

Clear-Host

while (($_pid = $null) -eq $null) {

    Write-Host "Getting PID ..."

    $_pid = Get-PID

}

Clear-Host

# Clear Buffer
& $ADB logcat -c

# Start Logcat
& $ADB logcat `
    "--pid=$_pid" `
    -v color 
