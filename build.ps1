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

    taskkill.exe /im java.exe /f
    taskkill.exe /im adb.exe /f

    Remove-Item `
        ".gradle" `
        -Force -Recurse -Verbose

    Get-ChildItem -Path . -Directory -Filter "build" -Recurse `
        | Remove-Item -Recurse -Force

    Remove-Item `
        "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'youtubeapi'
Add-YuliskovPkg 'mediaserviceinterfaces'
Add-YuliskovPkg 'sharedutils'

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS @args
