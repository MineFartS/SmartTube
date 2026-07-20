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

    Remove-Item `
        ".gradle" `
        -Force -Recurse -Verbose

    Remove-Item `
        ".build" `
        -Force -Recurse -Verbose

    Remove-Item `
        "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose

    Remove-Item "src\main\assets\nsigsolver" -Recurse -Force

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

if (-not (Test-Path "src\main\assets\nsigsolver\*.js")) {

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

Clear-Host

Invoke-Gradle @gARGS
