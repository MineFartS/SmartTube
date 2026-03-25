Import-Module "$PSScriptRoot/module.psm1" -Force

Connect-ADB

Clear-Host

$_pid = & $ADB shell pidof org.smarttube.stable

# Clear Buffer
& $ADB logcat -c

# Start Logcat
& $ADB logcat `
    "--pid=$_pid" `
    -v color 
