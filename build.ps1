param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$gARGS = @()

if ($Force) {

    Stop-Process -Name "java*"

    Remove-Item "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose -ErrorAction SilentlyContinue

    git.exe submodule update --recursive --remote --force lib/yuliskov

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

Copy-Item `
    -Path 'patch\*' `
    -Destination 'lib\yuliskov' `
    -Force -Verbose -Recurse

if (Test-ADBConnection) {
    $gARGS += ":smarttubetv:installStstableDebug"
} else {
    $gARGS += ":smarttubetv:buildStstableDebug"
}

Clear-Host

Invoke-Gradle @gARGS
