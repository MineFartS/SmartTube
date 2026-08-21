Add-Type -AssemblyName System.Text.RegularExpressions

$ErrorActionPreference = 'Stop'

#==================================================

Set-Location $PSScriptRoot

$Env:JAVA_HOME = "$PSScriptRoot\lib\jdk17"
$Env:PATH += ";$Env:JAVA_HOME\bin"

$Env:ANDROID_SDK_ROOT = "$PSScriptRoot\lib\sdk"
$Env:PATH += ";$Env:ANDROID_SDK_ROOT\platform-tools"

git.exe submodule update --init --recursive --remote

if (-not (Test-Path "$Env:ANDROID_SDK_ROOT\.knownPackages")) {
    & "$Env:ANDROID_SDK_ROOT\Accept.ps1"
}

#==================================================

function Test-ADBConnection {

    $devices = adb.exe devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Invoke-Gradle ([Switch]$Yuliskov, [Parameter(ValueFromRemainingArguments)] $cmdargs) {
    & "$PSScriptRoot/lib/yuliskov/gradlew.bat" `
        @cmdargs `
        --max-workers=3 `
        --no-daemon
}

function Invoke-ADB ([Parameter(ValueFromRemainingArguments)] $cmdargs) {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        Write-Host "Enter Target IP Address or leave blank to skip"
        $IP = Read-Host 'Target IP Address'

        if ($IP -ne "") {

            adb.exe connect $IP

            while (-not (Test-ADBConnection)) {
                Write-Host 'Awaiting Connection ...'
                Start-Sleep 3
            }

        }
        
    }

    if ($cmdargs.Count -gt 0) {
        adb.exe @cmdargs
    }

}

function Invoke-Python ([Parameter(ValueFromRemainingArguments)] $cmdargs) {
    & "$PSScriptRoot\lib\py314\python.exe" @cmdargs
}

#==================================================

Export-ModuleMember `
    -Function * `
    -Variable *
