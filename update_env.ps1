
# Get the current Path
$Path = [Environment]::GetEnvironmentVariable("Path", "User")

$Dir = "C:\Users\$env:Username\AppData\Local\android\Sdk\platform-tools"

if ($Path.Contains($Dir)) {

    Write-Host 'Android Platform Tools already in Path'

} else {

    [Environment]::SetEnvironmentVariable("Path", "$Path;$Dir", "User")

    Write-Host 'Android Platform Tools added to Path'

}
