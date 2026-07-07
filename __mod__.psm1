
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

        New-Item 'aar' -ItemType Directory

        Set-Location "$lib/yuliskov/"

        Invoke-Gradle ":$($Name):assemble"

        Get-ChildItem -Path . -Filter "$Name*debug.aar" -Recurse `
            | Move-Item -Destination $Dst

        Set-Location $PSScriptRoot

    }

}

function Invoke-Gradle {
    param(
        [Parameter(ValueFromRemainingArguments)]
        $cmdargs
    )

    .\gradlew.bat @cmdargs

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

    & "$lib\py314\python.exe" @cmdargs

}

function Repair-Environment {

    Set-Location $PSScriptRoot

    git.exe submodule update --init --recursive --remote
    
    $Env:JAVA_HOME = $JAVA_HOME
    $Env:PATH += ";$JAVA_HOME/bin;$lib"
    
    if (-not (Test-Path "$ANDROID_SDK\.knownPackages")) {
        & "$ANDROID_SDK\Accept.ps1"
    }

}

Export-ModuleMember `
    -Function * `
    -Variable *
