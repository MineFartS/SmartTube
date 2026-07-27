param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$gARGS = @()

$YTSolver = "src\main\assets\yt.solver.js"

if ($Force) {

    Stop-Process -Name "java*"

    @(
        ".gradle", ".build", "aar", $YTSolver,
        "$env:USERPROFILE\.gradle\caches"
        
    ) | Remove-Item -Force -Recurse -Verbose -ErrorAction SilentlyContinue

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'mediaserviceinterfaces' '/MediaServiceCore/mediaserviceinterfaces/'
Add-YuliskovPkg 'youtubeapi' '/MediaServiceCore/youtubeapi/'
Add-YuliskovPkg 'sharedutils' '/SharedModules/sharedutils/'
Add-YuliskovPkg 'exoplayer-library-core' '/exoplayer-amzn-2.10.6/library/core/'

if (-not (Test-Path $YTSolver)) {

    Invoke-Python "lib\ejs\hatch_build.py"

    Copy-Item "lib\ejs\dist\yt.solver.js" $YTSolver

}

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
