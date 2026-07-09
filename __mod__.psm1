
$ANDROID_SDK = "$PSScriptRoot\lib\sdk"
$ADB = "$ANDROID_SDK\platform-tools\adb.exe"

$JAVA_HOME = "$PSScriptRoot\lib\jdk17"

$APP_ID = "app.smarttube.stable"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

$YuliskovMap = @{
    'private class' = 'public class'
    'internal class' = 'public class'
    'internal object class' = 'public object class'
    'internal abstract class' = 'public abstract class'
    'internal data class' = 'public data class'
    'internal open class' = 'public open class'
    'internal enum class' = 'public enum class'
    'internal interface' = 'public interface'
    'internal object' = 'public object'
}

function Add-YuliskovPkg ([String]$Name) {

    $Dst = "$PSScriptRoot/aar/$Name.aar"

    if (-not (Test-Path $Dst)) {

        Copy-Item "local.properties" "lib/yuliskov/local.properties" -Force

        New-Item 'aar' -ItemType Directory

        Set-Location "$PSScriptRoot/lib/yuliskov/"

        Get-ChildItem -Directory -Recurse -Filter $Name `
        | Get-ChildItem -Directory -Filter "build" -Recurse `
            | Remove-Item -Recurse -Force -Verbose

        Get-ChildItem -Directory -Recurse -Filter $Name `
        | Get-ChildItem -File -Recurse -Include '*.kt','*.java' `
        | ForEach-Object { $_
            $text = Get-Content $_.FullName
            foreach ($key in $YuliskovMap.Keys) {
                $text = $text -creplace $key, $YuliskovMap[$key]
            }
            Set-Content -Value $text -Path $_.FullName
        }

        Invoke-Gradle ":$($Name):assemble"

        Get-ChildItem -Path . -Filter "$Name*debug.aar" -Recurse `
            | Sort-Object { $_.Name -like "*stbeta*" } -Descending `
            | Select-Object -First 1 `
            | Move-Item -Destination $Dst -Verbose

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

    & "$PSScriptRoot\lib\py314\python.exe" @cmdargs

}

function Repair-Environment {

    Set-Location $PSScriptRoot

    git.exe submodule update --init --recursive
    
    if (-not (Test-Path "$ANDROID_SDK\.knownPackages")) {
        $env:JAVA_HOME = $JAVA_HOME
        & "$ANDROID_SDK\Accept.ps1"
    }

    Write-Output "org.gradle.java.home=$JAVA_HOME" > 'local.properties'
    Write-Output "sdk.dir=$ANDROID_SDK" >> 'local.properties'

    (Get-Content -Path "local.properties") -replace '\\', '/' | Set-Content -Encoding utf8 "local.properties"

    Copy-Item "lib/yuliskov/smarttubetv/google-services.json" "google-services.json"

    $Env:JAVA_HOME = $JAVA_HOME
    $Env:PATH += ";$JAVA_HOME/bin"

}

Export-ModuleMember `
    -Function * `
    -Variable *
