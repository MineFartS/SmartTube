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

Add-YuliskovPkg 'exoplayer-library' '/exoplayer-amzn-2.10.6/library/all/'
Add-YuliskovPkg 'exoplayer-extension-leanback' '/exoplayer-amzn-2.10.6/extensions/leanback/'
Add-YuliskovPkg 'exoplayer-extension-mediasession' '/exoplayer-amzn-2.10.6/extensions/mediasession/'

Add-YuliskovPkg 'fragment-1.1.0' '/fragment-1.1.0/'
Add-YuliskovPkg 'leanback-1.0.0' '/leanback-1.0.0/'
Add-YuliskovPkg 'common' '/common/'
Add-YuliskovPkg 'sharedutils' '/SharedModules/sharedutils/'
Add-YuliskovPkg 'mediaserviceinterfaces' '/MediaServiceCore/mediaserviceinterfaces/'
Add-YuliskovPkg 'youtubeapi' '/MediaServiceCore/youtubeapi/'
Add-YuliskovPkg 'chatkit' '/chatkit/'

Add-YuliskovPkg 'commons-io-2.8.0' '/SharedModules/commons-io-2.8.0/'
Add-YuliskovPkg 'j2v8' '/SharedModules/j2v8/'

#if (-not (Test-Path $YTSolver)) {

#    Invoke-Python "lib\ejs2\hatch_build.py"

#    Copy-Item "lib\ejs2\dist\yt.solver.js" $YTSolver

#}

if (Test-ADBConnection) {
    $gARGS += ":smarttubetv:installStstableDebug"
} else {
    $gARGS += ":smarttubetv:build"
}

Clear-Host

Invoke-Gradle @gARGS
