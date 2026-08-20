Import-Module "$PSScriptRoot/__mod__.psm1" -Force

Invoke-ADB

Clear-Host

$HidePats = @(
    'fromNullable'
    'concurrent copying GC'
    'A resource failed to call close'
    'TRACE Starting certificate trust'
    'Accessing hidden (field|method)'
    'Cancelling event due to'
    'Verification of(.*)took(.*)ms'
    'Late-enabling -Xcheck:jni'
    "Unknown chunk type '200'"
    'AWV - '
    'Access token is null!'
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
    'InputMethodManager'
    'cr_ApkInfo'
    'HDRVideoUtils'
    'cr_LibraryLoader'
    'cr_CachingUmaRecorder'
    'cr_CombinedPProvider'
    'cr_PolicyProvider'
    'cr_WVCFactoryProvider'
    'cr_AppResProvider'
    'art'
    'FrameworkJumpTable'
    'MTK_GRALLOC'
    'VMetricsFramework.Event'
)

Invoke-Python 'lib/pidcat/pidcat.py' `
    "minefarts.smarttube" `
    '--min-level' 'V' `
    '--regex' ('^(?!.*(' + ($HidePats -join '|') + '))') `
    @($HideTags | ForEach-Object { '--ignore-tag', $_ }) `
    '--clear'
