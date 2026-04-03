Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Repair-Environment

Connect-ADB

Clear-Host

# Execute Gradle
& $JAVA `
    '-classpath' ".\pkg\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug"
