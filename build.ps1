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
