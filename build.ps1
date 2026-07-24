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

if (-not (Test-Path "src\main\assets\nsigsolver\*.js")) {

    Set-Location "$lib\yuliskov\MediaServiceCore\youtubeapi\src\main\assets\nsigsolver\"

    Get-ChildItem -Filter "*.js" -File | ForEach-Object {
        Import-NSigSolver $_
    }

    Set-Location "$lib\ejs"
    
    Invoke-Deno install
    $env:EJS_BUILD_INSTALLER = "deno"

    Invoke-Python "hatch_build.py"

    Get-ChildItem -Path "dist" -Filter "*.js" -File | ForEach-Object {
        Import-NSigSolver $_
    }
    
    Set-Location $PSScriptRoot

}

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
