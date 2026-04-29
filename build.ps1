param (
    [Switch] $Clean,
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

if ($Clean) {
    Invoke-Gradle 'clean'
}

if ($Force) {
    taskkill.exe /im java.exe /f
}

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
