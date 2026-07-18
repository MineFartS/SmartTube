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

    Remove-Item "$env:USERPROFILE\.gradle\caches" -Recurse -Force -Verbose
    Remove-Item '.gradle' -Recurse -Force  -Verbose
    Remove-Item 'aar' -Recurse -Force  -Verbose
    Remove-Item "src\main\assets" -Recurse -Force -Verbose
    
    Get-ChildItem -Directory -Filter "build" -Recurse `
        | Remove-Item -Recurse -Force -Verbose
    
    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Add-YuliskovPkg 'youtubeapi'
Add-YuliskovPkg 'mediaserviceinterfaces'
Add-YuliskovPkg 'sharedutils'

if (-not (Test-Path "src\main\assets\nsigsolver\yt.solver.lib.js")) {

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
