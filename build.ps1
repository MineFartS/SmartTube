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

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

    # Find the latest lib/yuliskov release force-checkout to it 
    git -C lib/yuliskov fetch --tags
    git -C lib/yuliskov checkout $(git -C lib/yuliskov describe --tags --abbrev=0 $(git -C lib/yuliskov rev-list --tags --max-count=1))
    git -C lib/yuliskov submodule update --init --recursive --force

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
