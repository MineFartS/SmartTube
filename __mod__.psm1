Add-Type -AssemblyName System.IO.Compression.FileSystem

$lib = "$PSScriptRoot\lib"

$ANDROID_SDK = "$lib\sdk"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JAVA_HOME = "$lib\jdk17"

$AsmJar = "$lib\asm-9.7.1.jar"

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

        # Compile Transformer.java
        if (-not (Test-Path "$PSScriptRoot/src/Transformer.class")) {
            Invoke-JavaC '-cp' $AsmJar '-d' "$PSScriptRoot/src" "$PSScriptRoot/src/Transformer.java"
        }

        $tasks = Invoke-Gradle ":${Name}:tasks" '--all'

        # Get flavor name
        $flavor = "Debug"
        if ($tasks | Select-String "StbetaDebug") {
            $flavor = "StbetaDebug"
        }

        # Build Class Files
        Invoke-Gradle ":$($Name):compile$($flavor)JavaWithJavac"
        if ($tasks | Select-String "compile*kotlin") {
            Invoke-Gradle ":$($Name):compile$($flavor)Kotlin"
        }

        $projectDir = Get-ChildItem $Name -Directory -Recurse | Where-Object { Test-Path (Join-Path $_.FullName "build") } | Select-Object -First 1

        # Transform Class Files
        Invoke-Java "-cp" "$AsmJar;$PSScriptRoot/src" "Transformer" $projectDir.FullName
        
        # Build the AAR binary
        Invoke-Gradle ":$($Name):assemble$flavor"

        # Relocate the AAR Binary
        $projectDir | Get-ChildItem -Filter "*debug.aar" -Recurse | Move-Item -Destination $Dst -Verbose

        Set-Location $PSScriptRoot
    }
}

function Invoke-Java([Parameter(ValueFromRemainingArguments)]$cmdargs) {
    & "$JAVA_HOME/bin/java.exe" @cmdargs
}

function Invoke-JavaC([Parameter(ValueFromRemainingArguments)]$cmdargs) {
    & "$JAVA_HOME/bin/javac.exe" @cmdargs
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
    
    $Env:JAVA_HOME = $JAVA_HOME
    $Env:PATH += ";$JAVA_HOME/bin;$lib"
    
    & "$ANDROID_SDK\Accept.ps1"

    Write-Output "org.gradle.java.home=$JAVA_HOME" > 'local.properties'
    Write-Output "sdk.dir=$ANDROID_SDK" >> 'local.properties'

    (Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"
    
}

Export-ModuleMember `
    -Function * `
    -Variable *
