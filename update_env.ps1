
# Get the current Path
$Path = [Environment]::GetEnvironmentVariable("Path", "Machine")

# Add Android Platform Tools to Path
$Path += ";C:\Users\$env:Username\AppData\Local\android\Sdk\platform-tools"

# Add Java Binaries to path
$Path += ";C:\Program Files\Java\jdk-14\bin"

# Save the updated Path
[Environment]::SetEnvironmentVariable("Path", $Path, "Machine")

# Set the 'JAVA_HOME' variable
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-14", "Machine")
