Import-Module "$PSScriptRoot/module.psm1" -Force

Set-Location $PSScriptRoot

Connect-ADB

Clear-Host

# Set Android SDK Path
Set-SDK "C:\Users\$env:USERNAME\AppData\Local\Android\sdk\"

# Update Version Name
(Get-Content "$PSScriptRoot\smarttubetv\build.gradle") `
    -Replace 'versionName\s+".*"', "versionName `"$(Get-Date -Format "yy.MM.dd")`"" `
    | Set-Content "$PSScriptRoot\smarttubetv\build.gradle"

# Execute Gradle
& $JAVA `
    '-classpath' ".\gradle\wrapper\gradle-wrapper.jar" `
    'org.gradle.wrapper.GradleWrapperMain' `
    "clean" "installStstableDebug"
