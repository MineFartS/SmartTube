Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Connect-ADB

while ($true) {

    Clear-Host

    $_pid = Get-PID

    if ($null -ne $_pid) {

        # Clear Buffer
        Invoke-Logcat -c

        if ($null -eq $_pid) {
            Invoke-Logcat `
                "--pid=$($_pid)" `
                -v color 
        } else {
            Invoke-Logcat `
                -v color
        }

    }

    Pause

}