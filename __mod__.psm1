Add-Type -AssemblyName System.IO.Compression.FileSystem

$lib = "$PSScriptRoot\lib"

$ANDROID_SDK = "$lib\sdk"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JAVA_HOME = "$lib\jdk17"

$APP_ID = "app.smarttube.stable"

function Test-ADBConnection {
    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
}

function Add-YuliskovPkg ([String]$Name) {

    $Dst = "$PSScriptRoot/aar/$Name.aar"

    if (-not (Test-Path $Dst)) {
                
        Copy-Item "local.properties" "lib/yuliskov/local.properties" -Force
        New-Item 'aar' -ItemType Directory -ErrorAction SilentlyContinue
        
        Set-Location "$PSScriptRoot/lib/yuliskov/"

        Get-ChildItem -Directory -Recurse -Filter $Name | ForEach-Object {
            Invoke-Python "$PSScriptRoot/lib/publicizer/run.py" $_.FullName
        }
        
        # Build the AAR binary
        Invoke-Gradle ":$($Name):assembleDebug"

        # Relocate the AAR Binary
        Get-ChildItem -Filter "$Name*debug.aar" -Recurse `
            | Sort-Object { $_.Name -like "*stbeta*" } -Descending `
            | Select-Object -First 1 `
            | Move-Item -Destination $Dst -Verbose

        Set-Location $PSScriptRoot
    }
}

function Invoke-Gradle([Parameter(ValueFromRemainingArguments)]$cmdargs) {
    .\gradlew.bat @cmdargs '--no-daemon'
}

function Invoke-Deno([Parameter(ValueFromRemainingArguments)]$cmdargs) {
    & "$lib\deno.exe" @cmdargs
}

function Invoke-ADB([Parameter(ValueFromRemainingArguments)]$cmdargs) {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        Write-Host "Enter Target IP Address or leave blank to skip install"
        $IP = Read-Host 'Target IP Address'

        if ($IP -eq "") { return $false }

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {
            Write-Host 'Awaiting Connection ...'
            Start-Sleep -Seconds 2
        }
        
    }

    if ($cmdargs.Count -gt 0) {
        & $ADB @cmdargs
    }

}

function Invoke-Python([Parameter(ValueFromRemainingArguments)]$cmdargs) {
    & "$PSScriptRoot\lib\py314\python.exe" @cmdargs
}

function Repair-Environment {

    Set-Location $PSScriptRoot

    git.exe submodule update --init --recursive --remote
    git.exe submodule update --init --recursive --remote --force lib/yuliskov
    
    $Env:JAVA_HOME = $JAVA_HOME
    $Env:PATH += ";$lib"
    $Env:PATH += ";$JAVA_HOME/bin"
    $Env:PATH += ";$lib/kotlinc/bin"
    
    & "$ANDROID_SDK\Accept.ps1"

    Write-Output "org.gradle.java.home=$JAVA_HOME" > 'local.properties'
    Write-Output "sdk.dir=$ANDROID_SDK" >> 'local.properties'

    (Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"

    Invoke-Python '-m' 'pip' 'install' 'philh_myftp_biz==2026.07.18' 'tree-sitter' 'tree-sitter-kotlin'
    
}

Export-ModuleMember `
    -Function * `
    -Variable *
