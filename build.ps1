param (
    [Switch] $Clean,
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

[System.Collections.ArrayList] $gARGS = @()

if ($Clean) {
    Invoke-Gradle 'clean'
}

if ($Force) {
    taskkill.exe /im java.exe /f
    $gARGS += '--no-daemon'
}

Clear-Host

if (Connect-ADB) {
    $gARGS.Insert(0, ":installDebug")
} else {
    $gARGS.Insert(0, ":build")
}

Clear-Host

Invoke-Gradle @gARGS
