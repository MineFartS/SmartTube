Import-Module "$PSScriptRoot/module.psm1" -Function Connect-ADB

Set-Location $PSScriptRoot

Connect-ADB

Clear-Host

# Execute Gradle
& "$env:JAVA_HOME/bin/java.exe" `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug" `
    "--warning-mode" "all"
