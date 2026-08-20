param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$gARGS = @()

if ($Force) {

    Stop-Process -Name "java*"

    @(
        ".gradle", "build",
        "$env:USERPROFILE\.gradle\caches"
    ) | Remove-Item -Force -Recurse -Verbose -ErrorAction SilentlyContinue

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'exoplayer-library-core' '/exoplayer-amzn-2.10.6/library/core/'
Add-YuliskovPkg 'exoplayer-library-ui' '/exoplayer-amzn-2.10.6/library/ui/'
Add-YuliskovPkg 'exoplayer-library-dash' '/exoplayer-amzn-2.10.6/library/dash/'
Add-YuliskovPkg 'exoplayer-library-sabr' '/exoplayer-amzn-2.10.6/library/sabr/'
Add-YuliskovPkg 'exoplayer-library-hls' '/exoplayer-amzn-2.10.6/library/hls/'
Add-YuliskovPkg 'exoplayer-library-smoothstreaming' '/exoplayer-amzn-2.10.6/library/smoothstreaming/'

Add-YuliskovPkg 'exoplayer-extension-leanback' '/exoplayer-amzn-2.10.6/extensions/leanback/'
Add-YuliskovPkg 'exoplayer-extension-mediasession' '/exoplayer-amzn-2.10.6/extensions/mediasession/'
Add-YuliskovPkg 'exoplayer-extension-okhttp' '/exoplayer-amzn-2.10.6/extensions/okhttp/'

Add-YuliskovPkg 'fragment-1.1.0' '/fragment-1.1.0/'
Add-YuliskovPkg 'leanback-1.0.0' '/leanback-1.0.0/'
Add-YuliskovPkg 'common' '/common/'
Add-YuliskovPkg 'sharedutils' '/SharedModules/sharedutils/'
Add-YuliskovPkg 'appupdatechecker2' '/SharedModules/appupdatechecker2/'
Add-YuliskovPkg 'mediaserviceinterfaces' '/MediaServiceCore/mediaserviceinterfaces/'
Add-YuliskovPkg 'youtubeapi' '/MediaServiceCore/youtubeapi/'
Add-YuliskovPkg 'chatkit' '/chatkit/'

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
