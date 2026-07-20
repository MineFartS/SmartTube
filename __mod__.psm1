
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

function Invoke-Gradle {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

    ."$PSScriptRoot\gradlew.bat" @cmdargs

}

function Invoke-Deno {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

    & "$lib\deno.exe" @cmdargs

}

function Invoke-ADB {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

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

function Invoke-Python {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

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
