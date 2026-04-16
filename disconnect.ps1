Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Clear-Host

Remove-Cache

& $ADB disconnect
