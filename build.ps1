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

    Get-ChildItem -Directory -Filter "build" -Recurse `
        | Remove-Item -Recurse -Force

    Remove-Item 'aar' -Recurse -Force
    Remove-Item "src\main\assets" -Recurse -Force

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Copy-Item `
    "src\main\java\com\liskovsoft\youtubeapi\app\nsigsolver\impl\V8ChallengeProvider.kt" `
    "lib\yuliskov\MediaServiceCore\youtubeapi\src\main\java\com\liskovsoft\youtubeapi\app\nsigsolver\impl\V8ChallengeProvider.kt" `
    -Force -Verbose

Add-YuliskovPkg 'youtubeapi'
Add-YuliskovPkg 'mediaserviceinterfaces'
Add-YuliskovPkg 'sharedutils'

Copy-Item `
    "lib\yuliskov\MediaServiceCore\youtubeapi\src\main\assets\*" `
    "src\main\assets" `
    -Recurse -Verbose

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Invoke-Gradle @gARGS @args
