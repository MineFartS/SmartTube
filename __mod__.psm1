Add-Type -AssemblyName System.Text.RegularExpressions

$lib = "$PSScriptRoot\lib"

$ANDROID_SDK = "$lib\sdk"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JAVA_HOME = "$lib\jdk17"

$APP_ID = "minefarts.smarttube"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

$AccessPatterns = @(
    # Classes
    '\b(private|protected|internal)(?=\s+(?:(?:abstract|sealed|data|enum|open|inner|final|synchronized)\s+)*(?:class|interface|object)\b)',

    # Functions
    '\b(private|protected|internal)(?=\s+(?:(?:synchronized|final|abstract|inline|external|tailrec|operator|infix)\s+)*(?:fun|void\s+\w+|[\w<>\[\]]+\s+\w+(?=\s*\()))(?!\s+(?:[^\{]*?\b(?:open|override)\b))'
)

function Add-YuliskovPkg ([String]$Name) {

    $Dst = "$PSScriptRoot/aar/$Name.aar"

    if (-not (Test-Path $Dst)) {

        git.exe submodule update --init --recursive --remote --force lib/yuliskov

        Copy-Item "local.properties" "lib/yuliskov/local.properties" -Force

        New-Item 'aar' -ItemType Directory -ErrorAction SilentlyContinue

        Set-Location "$PSScriptRoot/lib/yuliskov/"

        $projectDir = Get-ChildItem -Directory -Recurse -Filter $Name
        
        $projectDir | ForEach-Object {
            Remove-Item "$_\src\main\res" -Force -Recurse
        }

        $projectDir | Get-ChildItem -File -Recurse | Where-Object Extension -match 'kt|java' | ForEach-Object { $_
            $text = Get-Content $_.FullName -Raw
            $AccessPatterns | ForEach-Object {
                $text = [regex]::Replace($text, $_, 'public')
            }
            Set-Content -Value $text -Path $_.FullName
        }

        Invoke-Gradle ":$($Name):assemble"

        $projectDir | Get-ChildItem -Filter "$Name*debug.aar" -Recurse `
            | Sort-Object { $_.Name -like "*stbeta*" } -Descending `
            | Select-Object -First 1 `
            | Move-Item -Destination $Dst -Verbose

        Set-Location $PSScriptRoot

    }

}

function Invoke-Gradle ([Parameter(ValueFromRemainingArguments)] $cmdargs) {
    .\gradlew.bat @cmdargs
}

function Invoke-Deno ([Parameter(ValueFromRemainingArguments)] $cmdargs) {
    & "$lib\deno.exe" @cmdargs
}

function Invoke-ADB ([Parameter(ValueFromRemainingArguments)] $cmdargs) {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        Write-Host "Enter Target IP Address or leave blank to skip install"
        $IP = Read-Host 'Target IP Address'

        if ($IP -eq "") {return $false}

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {
            Write-Host 'Awaiting Connection ...'
        }
        
    }

    if ($cmdargs.Count -gt 0) {
        & $ADB @cmdargs
    }

}

function Invoke-Python ([Parameter(ValueFromRemainingArguments)] $cmdargs) {
    & "$PSScriptRoot\lib\py314\python.exe" @cmdargs
}

function Repair-Environment {

    Set-Location $PSScriptRoot

    git.exe submodule update --init --recursive --remote
    
    $Env:JAVA_HOME = $JAVA_HOME
    $Env:PATH += ";$JAVA_HOME/bin;$lib"

    Write-Output "org.gradle.java.home=$JAVA_HOME" > 'local.properties'
    Write-Output "sdk.dir=$ANDROID_SDK" >> 'local.properties'
    (Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"

    & "$ANDROID_SDK\Accept.ps1"

}

Export-ModuleMember `
    -Function * `
    -Variable *
