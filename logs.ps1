Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Connect-ADB

while ($true) {

    Clear-Host

    $_pid = Get-PID

    if ($null -ne $_pid) {

        # Clear Buffer
        & $ADB logcat -c

        # Start Logcat
        & $ADB logcat `
            "--pid=$($_pid)" `
            -v color 

    }

    Pause

}