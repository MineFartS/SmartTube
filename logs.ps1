Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$HidePats = @(
    'fromNullable'
    'concurrent copying GC'
    'A resource failed to call close'
    'TRACE Starting certificate trust'
    'Accessing hidden (field|method)'
)

$HideTags = @(
    'okhttp.OkHttpClient'
    'ExoPlayerImpl'
    'chatty'
    'CameraManagerGlobal'
    'AudioTrack'
    'MediaCodecRenderer'
    'AmazonKeyEventLogging'
    'StaticLayout'
    'cr_VAUtil'
    'Chrome_InProcGp'
    'IMGSRV'
    'cr_AWVDeploymentClient'
    'WebViewExtAmazonConfig'
    'WebViewExtAmazon'
    'VideoCapabilities'
    'Choreographer'
    'OpenGLRenderer'
    'ConfigStore'
    'ion'
    'PerfStatsManager'
    'MultiDex'
    'libc'
)

Invoke-Python 'lib/pidcat/pidcat.py' `
    "minefarts.smarttube" `
    '--min-level' 'V' `
    '--sdk' $Env:ANDROID_SDK_ROOT `
    '--regex' ('^(?!.*(' + ($HidePats -join '|') + '))') `
    @($HideTags | ForEach-Object { '--ignore-tag', $_ }) `
    '--clear'
