
$Path = [Environment]::GetEnvironmentVariable("Path", "User")

$dirs = @(
    "C:\Users\$env:Username\AppData\Local\android\Sdk\platform-tools",
    "C:\Program Files\Java\jdk-14\bin"
)

foreach ($dir in $dirs) {

    if ($Path.Contains($dir)) {

        Write-Host "Already in Path: $dir"

    } else {

        $Path += ";$dir"

        Write-Host "Added to Path: $dir"

    }

}

Write-Host 'Saving Updated Path'
[Environment]::SetEnvironmentVariable("Path", $Path, "User")

Write-Host 'Updating JAVE_HOME'
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-14\", "User")