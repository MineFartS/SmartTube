Import-Module "$PSScriptRoot/module.psm1" -Function Connect-ADB

Set-Location $PSScriptRoot

Connect-ADB

Clear-Host

#
(Get-Content "$PSScriptRoot\smarttubetv\build.gradle") `
    -Replace 'versionName\s+".*"', "versionName `"$(Get-Date -Format "yy.MM.dd")`"" `
    | Set-Content "$PSScriptRoot\smarttubetv\build.gradle"

# Execute Gradle
& "$env:JAVA_HOME/bin/java.exe" `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug" `
    "--warning-mode" "all"
