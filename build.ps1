param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Invoke-ADB

Clear-Host

Invoke-Gradle --stop

$gARGS = @()

if ($Force) {

    Stop-Process -Name "java*"

    @(
        ".gradle", ".build", "aar",
        "$env:USERPROFILE\.gradle\caches",
        "src\main\assets\nsigsolver"
    ) | Remove-Item -Force -Recurse -Verbose -ErrorAction SilentlyContinue

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'youtubeapi'
Add-YuliskovPkg 'mediaserviceinterfaces'
Add-YuliskovPkg 'sharedutils'

$YTSolver = "src\main\assets\yt.solver.js"

if (-not (Test-Path $YTSolver)) {

    Invoke-Python "$lib\ejs\hatch_build.py"

    New-Item "src\main\assets\nsigsolver\" -ItemType Directory

    Copy-Item "$lib\ejs\dist\yt.solver.js" $YTSolver

}

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
