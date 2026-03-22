
$ADB = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"

$JAVA = "C:\Program Files\Java\jdk-14\bin\java.exe"

function Test-ADBConnection {

    $devices = & $ADB devices `
        | Select-String -NotMatch "List of devices attached" `
        | Where-Object { $_.ToString().Trim().Length -gt 0 }

    return $devices.Count -gt 0
    
}

function Connect-ADB {

    if (-not (Test-ADBConnection)) {

        Write-Host "No ADB device is connected"
        $IP = Read-Host 'Target IP Address'

        & $ADB connect $IP

        while (-not (Test-ADBConnection)) {

            Write-Host 'Awaiting Connection ...'

        }
        
    }

}

function Set-SDK {

    param ([String]$Path)

    $Path = $Path.Replace('\', '\\')

    "sdk.dir = $Path" ` | Set-Content "$PSScriptRoot\local.properties"

}

# Function to highlight specific keywords
function Start-ColorProcess {
    param(
        [System.Collections.ArrayList] $Arguments,
        $ColorMap
    )

    $_cmd = $Arguments[0]
    $_args = $Arguments | Select-Object -Skip 1

    & $_cmd @_args | ForEach-Object { 
    
        $Line = $_
    
        # Simple regex replace to wrap keywords in a delimiter, then split
        $pattern = "(" + (($ColorMap.Keys | ForEach-Object { [regex]::Escape($_) }) -join "|") + ")"
        $parts = [regex]::Split($Line, $pattern)
        
        foreach ($part in $parts) {
            if ($ColorMap.ContainsKey($part)) {
                Write-Host $part -NoNewline -ForegroundColor $ColorMap[$part]
            } else {
                Write-Host $part -NoNewline -ForegroundColor White
            }
        }

        Write-Host # Newline

    }

}

Export-ModuleMember `
    -Function * `
    -Variable *
