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

Copy-Item `
    "lib/yuliskov/smarttubetv/google-services.json" `
    "google-services.json" `
    -Force -Verbose

Add-YuliskovPkg 'youtubeapi'
Add-YuliskovPkg 'mediaserviceinterfaces'
Add-YuliskovPkg 'sharedutils'

if (-not (Test-Path "src\main\assets\nsigsolver\yt.solver.lib.js")) {

    Copy-Item `
        "lib\yuliskov\MediaServiceCore\youtubeapi\src\main\assets\*" `
        "src\main\assets" `
        -Recurse -Verbose

    Push-Location "$lib\ejs"
    
    Invoke-Deno install
    $env:EJS_BUILD_INSTALLER = "deno"

    Invoke-Python "hatch_build.py"

    Get-ChildItem -Path "dist" -Filter "*.js" -File -Recurse | ForEach-Object {

        Invoke-Deno run -A `
            npm:@swc/cli@0.8.1/swc `
            $_.FullName `
            '-o' "$PSScriptRoot\src\main\assets\nsigsolver\$($_.Name)"
                
    }
        
    Pop-Location
    
}

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Invoke-Gradle @gARGS @args
