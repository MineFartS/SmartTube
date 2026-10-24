param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$gARGS = @('-p', "$PSScriptRoot\smarttubetv")

if ($Force) {

    Stop-Process -Name "java*"

    Remove-Item `
        -Path "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose `
        -ErrorAction SilentlyContinue

    git.exe submodule update --init --recursive --force lib/yuliskov

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
