param (
    [Switch] $Clear
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

if ($Clear) {Remove-Cache}

Clear-Host

if (Connect-ADB) {
    Invoke-Gradle ":SmartTubeApp:installDebug"
} else {
    Invoke-Gradle ":SmartTubeApp:build"
}