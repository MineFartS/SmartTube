param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$gARGS = @('-p', "$PSScriptRoot\smarttubetv")

if ($Force) {

    Stop-Process -Name "java*"

    Remove-Item `
        -Path "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose `
        -ErrorAction SilentlyContinue

    git.exe submodule update --init --recursive --force lib/yuliskov

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'exoplayer-library' '/exoplayer-amzn-2.10.6/library/all/'
Add-YuliskovPkg 'exoplayer-library-core' '/exoplayer-amzn-2.10.6/library/core/'
Add-YuliskovPkg 'exoplayer-library-ui' '/exoplayer-amzn-2.10.6/library/ui/'
Add-YuliskovPkg 'exoplayer-library-dash' '/exoplayer-amzn-2.10.6/library/dash/'
Add-YuliskovPkg 'exoplayer-library-sabr' '/exoplayer-amzn-2.10.6/library/sabr/'
Add-YuliskovPkg 'exoplayer-library-hls' '/exoplayer-amzn-2.10.6/library/hls/'
Add-YuliskovPkg 'exoplayer-library-smoothstreaming' '/exoplayer-amzn-2.10.6/library/smoothstreaming/'

Add-YuliskovPkg 'exoplayer-extension-leanback' '/exoplayer-amzn-2.10.6/extensions/leanback/'
Add-YuliskovPkg 'exoplayer-extension-mediasession' '/exoplayer-amzn-2.10.6/extensions/mediasession/'
Add-YuliskovPkg 'exoplayer-extension-okhttp' '/exoplayer-amzn-2.10.6/extensions/okhttp/'
Add-YuliskovPkg 'exoplayer-extension-cronet' '/exoplayer-amzn-2.10.6/extensions/cronet/'

Add-YuliskovPkg 'fragment-1.1.0' '/fragment-1.1.0/'
Add-YuliskovPkg 'leanback-1.0.0' '/leanback-1.0.0/'
Add-YuliskovPkg 'sharedutils' '/SharedModules/sharedutils/'
Add-YuliskovPkg 'appupdatechecker2' '/SharedModules/appupdatechecker2/'
Add-YuliskovPkg 'mediaserviceinterfaces' '/MediaServiceCore/mediaserviceinterfaces/'
Add-YuliskovPkg 'youtubeapi' '/MediaServiceCore/youtubeapi/'
Add-YuliskovPkg 'chatkit' '/chatkit/'
Add-YuliskovPkg 'leanbackassistant' '/leanbackassistant/'
Add-YuliskovPkg 'slidableactivity' '/slidableactivity/'
Add-YuliskovPkg 'doubletapplayerview' '/doubletapplayerview/'
Add-YuliskovPkg 'filepicker-lib' '/filepicker-lib/'

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
