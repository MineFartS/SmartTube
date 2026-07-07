param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Write-Output "org.gradle.java.home=$JAVA_HOME" > 'local.properties'
Write-Output "sdk.dir=$ANDROID_SDK" >> 'local.properties'
(Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"

Copy-Item "lib/yuliskov/smarttubetv/google-services.json" "google-services.json"

Invoke-ADB

Clear-Host

Invoke-Gradle --stop

$gARGS = @()

if ($Force) {

    taskkill.exe /im java.exe /f
    taskkill.exe /im adb.exe /f

    Invoke-ADB

    Remove-Item `
        ".gradle" `
        -Force -Recurse -Verbose

    Get-ChildItem -Path . -Directory -Filter "build" -Recurse `
        | Remove-Item -Recurse -Force

    Remove-Item `
        "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose

    # Recompile js yt solvers
    Set-Location "$lib/ejs"
    & "$lib\deno.exe" install
    $env:EJS_BUILD_BUNDLER = "$lib\deno.exe"
    Invoke-Python "hatch_build.py"
    Copy-Item "dist\*" "$lib\yuliskov\MediaServiceCore\youtubeapi\src\main\assets\nsigsolver\"
    Set-Location $PSScriptRoot
    
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
