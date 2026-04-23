param (
    [Switch] $Clear
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

if ($Clear) {Remove-Cache}

Clear-Host

if (Connect-ADB) {
    try {
        Invoke-Gradle ":SmartTubeApp:installStstableDebug"
    } catch {
        Invoke-Gradle ":installDebug"
    }
} else {
    Invoke-Gradle ":build"
}