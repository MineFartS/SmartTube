param(
    [Switch] $Verbose
)

Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$Level = if ($Verbose) {"V"} else {"W"}

Invoke-Python 'lib/pidcat/pidcat.py' `
    "minefarts.smarttube" `
    '--min-level' $Level `
    '--sdk' $SDK `
    '--clear'
