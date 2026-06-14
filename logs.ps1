param(
    [Switch] $Verbose
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Invoke-ADB

Clear-Host

$Level = if ($Verbose) {"V"} else {"W"}

Invoke-Python lib/pidcat/pidcat.py `
    $APP_ID `
    --min-level $Level `
    --sdk $ANDROID_SDK `
    --clear
