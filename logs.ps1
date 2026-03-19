Import-Module "$PSScriptRoot/module.psm1" -Force

Connect-ADB

Clear-Host

$_pid = & $ADB shell pidof org.smarttube.stable

& $ADB logcat `
    "--pid=$_pid" `
    -v color 
