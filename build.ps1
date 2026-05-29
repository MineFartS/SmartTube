param (
    [Switch] $Force
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Clear-Host

$gARGS = @()

if ($Force) {

    Remove-Item `
        ".gradle" `
        -Force -Recurse -Verbose

    Remove-Item `
        "$env:USERPROFILE\.gradle\caches" `
        -Force -Recurse -Verbose

    $gARGS += 'clean'
    $gARGS += '--refresh-dependencies'

}

taskkill.exe /im java.exe /f

if (Test-ADBConnection) {
    $gARGS += ":installDebug"
} else {
    $gARGS += ":build"
}

Clear-Host

Invoke-Gradle @gARGS
