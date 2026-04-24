param (
    [Switch] $Clear
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

if ($Clear) {Remove-Cache}

Clear-Host

if (Connect-ADB) {
    Invoke-Gradle ":installDebug"
} else {
    Invoke-Gradle ":build"
}