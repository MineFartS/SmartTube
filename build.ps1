Import-Module "$PSScriptRoot/module.psm1" -Force

Set-Location $PSScriptRoot

Connect-ADB

Clear-Host

# Execute Gradle
& $JAVA `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug"
