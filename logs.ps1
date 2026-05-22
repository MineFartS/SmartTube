Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

while ($true) {

    Clear-Host

    $_pid = Invoke-ADB shell pidof $APP_ID

    if ($null -ne $_pid) {

        # Clear Buffer
        Invoke-ADB 'logcat' '-c'

        if ($null -eq $_pid) {
            Invoke-ADB 'logcat' -v color
        } else {
            Invoke-ADB 'logcat' `
                "--pid=$($_pid)" `
                '-v' 'color'
        }

    }

    Pause

}