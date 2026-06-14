Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Invoke-ADB

Clear-Host

Invoke-Python lib/pidcat/pidcat.py `
    $APP_ID `
    --sdk $ANDROID_SDK `
    --clear
