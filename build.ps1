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

Copy-Item `
    -Path 'patch\*' `
    -Destination 'lib\yuliskov' `
    -Force -Verbose -Recurse

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
