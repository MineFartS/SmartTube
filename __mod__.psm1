Add-Type -AssemblyName System.Text.RegularExpressions

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true

#==================================================

Set-Location $PSScriptRoot

$JDK = "$PSScriptRoot\lib\jdk17"
$SDK = "$PSScriptRoot\lib\sdk"

$ADB = "$SDK\platform-tools\adb.exe"

$Env:JAVA_HOME = $JDK
$Env:PATH += ";$JDK/bin"

git.exe submodule update --init --recursive --remote

Write-Output "org.gradle.java.home=$JDK" > 'local.properties'
Write-Output "sdk.dir=$SDK" >> 'local.properties'
(Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"

& "$SDK\Accept.ps1"

#==================================================

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

$AccessPatterns = @(
    # Classes
    '\b(private|protected|internal)(?=\s+(?:(?:abstract|sealed|data|enum|open|inner|final|synchronized)\s+)*(?:class|interface|object)\b)',

    # Kotlin Functions
    '\b(private|protected|internal)(?=\s+(?:(?:synchronized|final|abstract|inline|external|tailrec|operator|infix)\s+)*(?:fun|void\s+\w+|[\w<>\[\]]+\s+\w+(?=\s*\()))(?!\s+(?:[^\{]*?\b(?:open|override)\b))'
)

function Add-YuliskovPkg ([String]$Name, [String]$Path) {

    $Dst = "$PSScriptRoot/aar/$Name.aar"

    if (Test-Path $Dst) { return; }

    $Path = Get-Item "lib/yuliskov/$Path"

    git.exe submodule update --init --recursive --remote --force lib/yuliskov

    Copy-Item "local.properties" "lib/yuliskov/local.properties" -Force

    New-Item 'aar' -ItemType Directory -ErrorAction SilentlyContinue

    Remove-Item "$Path\src\main\res" -Force -Recurse -ErrorAction SilentlyContinue

    Get-ChildItem $Path -File -Recurse | Where-Object Extension -match 'kt|java' | ForEach-Object { $_
        $text = Get-Content $_.FullName -Raw
        $AccessPatterns | ForEach-Object {
            $text = [regex]::Replace($text, $_, 'public')
        }
        Set-Content -Value $text -Path $_.FullName
    }
    
    Invoke-Gradle -Yuliskov ":$($Name):assemble" "--no-daemon"

    Get-ChildItem $Path -Filter "$Name*debug.aar" -Recurse `
        | Sort-Object { $_.Name -like "*stbeta*" } -Descending `
        | Select-Object -First 1 `
        | Move-Item -Destination $Dst -Verbose

    Get-Item $Dst -ErrorAction Stop
}

function Invoke-Gradle ([Switch]$Yuliskov, [Parameter(ValueFromRemainingArguments)] $cmdargs) {
    if ($Yuliskov) {
        Push-Location "$PSScriptRoot/lib/yuliskov"
    } else {
        Push-Location $PSScriptRoot
    }

    .\gradlew.bat --stop
    .\gradlew.bat @cmdargs
    .\gradlew.bat --stop

    Pop-Location
}

function Invoke-ADB ([Parameter(ValueFromRemainingArguments)] $cmdargs) {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        Write-Host "Enter Target IP Address or leave blank to skip"
        $IP = Read-Host 'Target IP Address'

        if ($IP -ne "") {

            & $ADB connect $IP

            while (-not (Test-ADBConnection)) {
                Write-Host 'Awaiting Connection ...'
                Start-Sleep 3
            }

        }
        
    }

    if ($cmdargs.Count -gt 0) {
        & $ADB @cmdargs
    }

}

function Invoke-Python ([Parameter(ValueFromRemainingArguments)] $cmdargs) {
    & "$PSScriptRoot\lib\py314\python.exe" @cmdargs
}

#==================================================

Export-ModuleMember `
    -Function * `
    -Variable *
