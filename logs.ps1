Import-Module "$PSScriptRoot/module.psm1" -Function Connect-ADB

Connect-ADB

Clear-Host

$_pid = adb.exe shell pidof org.smarttube.stable

adb.exe logcat `
    "--pid=$_pid" `
    -v color 
