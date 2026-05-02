-keepclassmembers class com.bumptech.glide.Glide {
    <init>(...);
    void registerRequestManager(com.bumptech.glide.RequestManager);
}
-keepclassmembers class androidx.work.Logger {
    <clinit>(...);
    static java.lang.String tagWithPrefix(java.lang.String);
}
-keepclassmembers class androidx.leanback.widget.SearchOrbView {
    <init>(...);
}
-keepclassmembers class io.reactivex.plugins.RxJavaPlugins {
    static io.reactivex.Observable onAssembly(io.reactivex.Observable);
}
-keepclassmembers class androidx.core.content.ContextCompat {
    static int checkSelfPermission(android.content.Context, java.lang.String);
}
-keepclassmembers class androidx.work.WorkManager {
    static void initialize(android.content.Context, androidx.work.Configuration);
}
-keepclassmembers class com.liskovsoft.sharedutils.search.SearchTagStorage {
    <clinit>(...);
}
-keepclassmembers class androidx.leanback.widget.HorizontalGridView {
    <init>(...);
    android.graphics.LinearGradient mLowFadeShader;
}
-keepclassmembers class SmartTubeApp.ui.browse.video.MultipleRowsFragment$ItemViewClickedListener {
    <init>(...);
}
-keepclassmembers class com.liskovsoft.sharedutils.common.helpers.PostDataHelper {
    <clinit>(...);
    java.lang.String createQueryTV(java.lang.String);
}
-keepclassmembers class androidx.work.impl.utils.ForceStopRunnable {
    <init>(...);
}
-keepclassmembers class com.liskovsoft.sharedutils.channelgroups.ChannelGroupServiceImpl {
    <init>(...);
}
-keepclassmembers class androidx.leanback.widget.BrowseRowsFrameLayout {
    <init>(...);
}
-keepclassmembers class io.reactivex.android.schedulers.HandlerSaheduler {
    <init>(...);
}
-keepclassmembers class com.jayway.jsonpath.Configuration$ConfigurationBuilder {
    <init>(...);
    com.jayway.jsonpath.spi.mapper.MappingProvider mappingProvider;
    com.jayway.jsonpath.spi.json.JsonProvider jsonProvider;
}
-keepclassmembers class com.liskovsoft.sharedutils.mylogger.Log {
    static void d(java.lang.String, java.lang.Object, java.lang.Object[]);
}
-keepclassmembers class com.liskovsoft.sharedutils.service.YouTubeServiceManager {
    <init>(...);
}
-keepclassmembers class SmartTubeApp.ui.widgets.vineyard.TagCardView {
    <init>(...);
    android.widget.TextView mTagNameText;
    android.widget.ImageView mResultImage;
}
-keepclassmembers class SmartTubeApp.ui.widgets.marqueetextview.MarqueeTextView {
    <init>(...);
    float mDefaultMarqueeSpeed;
    float mMarqueeSpeedFactor;
}
-keepclassmembers class SmartTubeApp.ui.widgets.complexcardview.ComplexImageView {
    <init>(...);
    android.widget.ProgressBar mProgressBar;
    android.view.ViewGroup mProgressContainer;
    android.widget.FrameLayout mPreviewContainer;
}
-keepclassmembers class SmartTubeApp.ui.widgets.time.DateTimeView {
    <init>(...);
    boolean mIsDateEnabled;
    boolean mIsTimeEnabled;
}
-keepclassmembers class com.liskovsoft.sharedutils.helpers.Helpers {
    long sCachedRamSize;
    java.lang.String ARRAY_DELIM;
    java.lang.String DATA_DELIM;
    java.lang.String LEGACY_ARRAY_DELIM;
}
-keepclassmembers class com.google.android.exoplayer2.PlaybackParameters {
    <init>(...);
    float pitch;
}
-keepclassmembers class androidx.work.impl.constraints.trackers.Trackers {
    <init>(...);
}
-keepclassmembers class SmartTubeApp.ui.mod.leanback.misc.ProgressBarManager {
    <init>(...);
    android.view.View mProgressBarView;
}
-keepclassmembers class SmartTubeApp.misc.MotherActivity {
    <init>(...);
}
-keepclassmembers class SmartTubeApp.misc.TickleManager {
    <init>(...);
    SmartTubeApp.misc.TickleManager sInstance;
}
-keepclassmembers class androidx.core.content.FileProvider {
    <init>(...);
    void attachInfo(android.content.Context, android.content.pm.ProviderInfo);
}
-keepclassmembers class androidx.core.view.LayoutInflaterCompat {
    void setFactory2(android.view.LayoutInflater, android.view.LayoutInflater$Factory2);
}
-keepclassmembers class androidx.leanback.widget.SearchBar {
    <init>(...);
}
-keepclassmembers class com.liskovsoft.sharedutils.ServiceManager {
    <init>(...);
    com.liskovsoft.sharedutils.MediaItemService getMediaItemService();
}
# IncompatibleClassChangeError: androidx.recyclerview.widget.RecyclerView$Recycler
-keepclassmembers class androidx.recyclerview.widget.RecyclerView$Recycler {
    <init>(...);
}
# NoSuchFieldError: Attempted read of 32-bit non-primitive on field 'int android.content.res.Configuration.mnc'
-keepclassmembers class io.reactivex.disposables.CompositeDisposable {
    <init>(...);
}
# NoSuchMethodError: android.graphics.Canvas.drawCircle
-keepclassmembers class SmartTubeApp.ui.mod.leanback.misc.SeekBar {
    <init>(...);
    void onDraw(android.graphics.Canvas);
    int mKnobx;
    android.graphics.Paint mKnobPaint;
}
-keepclassmembers class okhttp3.OkHttpClient {
    <init>(...);
}
#-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
#    <init>(...);
#    void setNestedScrollingEnabled(boolean);
#}
-keepclassmembers class SmartTubeApp.utils.Utils {
    boolean isFormatSupported(SmartTubeApp.exoplayer.selector.track.MediaTrack);
}
-keepclassmembers class com.google.android.exoplayer2.video.VideoRendererEventListener {
    void onVideoDisabled(com.google.android.exoplayer2.decoder.DecoderCounters);
}
-keepclassmembers class SmartTubeApp.app.models.search.MediaServiceSearchTagProvider {
    <init>(...);
    boolean mIgnoreEmptyQuery;
}
-keepclassmembers class androidx.leanback.widget.GridLayoutManager {
    <init>(...);
}
-keepclassmembers class androidx.leanback.app.BaseRowSupportFragment {
    <init>(...);
    androidx.leanback.widget.VerticalGridView mVerticalGridView;
    androidx.leanback.widget.VerticalGridView getVerticalGridView();
}
-keepclassmembers class SmartTubeApp.app.models.playback.controllers.VideoStateController {
    <init>(...);
}
-keepclassmembers class SmartTubeApp.ui.browse.BrowseSectionFragmentFactory {
    <init>(...);
}
-keepclassmembers class androidx.room.RoomDatabase {
    <init>(...);
    androidx.sqlite.db.SupportSQLiteDatabase mDatabase;
}
-keepclassmembers class androidx.core.app.NotificationCompatBuilder {
    <init>(...);
    java.util.List mActionExtrasList;
}
-keepclassmembers class android.view.View {
    <init>(...);
    void setZ(float);
}
-keepclassmembers class SmartTubeApp.exoplayer.controller.ExoPlayerController {
    <init>(...);
    boolean containsMedia();
}
-keepclassmembers interface com.google.android.exoplayer2.Player$EventListener {
    void onPlaybackParametersChanged(com.google.android.exoplayer2.PlaybackParameters);
}
-keepclassmembers interface com.google.android.exoplayer2.Player {
    boolean isPlaying();
}
-keepclassmembers class com.liskovsoft.sharedutils.locale.LocaleContextWrapper {
    public static android.content.Context wrap(android.content.Context, java.util.Locale, android.util.DisplayMetrics);
}
-keepclassmembers class kotlin.collections.ArraysKt___ArraysKt {
    public static java.lang.Object firstOrNull(java.lang.Object[]);
}

# NOTE: Debug build: VirusTotal (Google: Detected, Ikarus: Trojan.AndroidOS.Agent)
#-keep class androidx.recyclerview.widget.RecyclerView { *; }
#-keep class com.google.android.exoplayer2.C { *; }
#-keep class kotlin.collections.CollectionsKt__CollectionsKt { *; }
#-keep class okhttp3.OkHttpClient$Builder { *; }
#-keep class androidx.leanback.widget.ItemBridgeAdapter { *; }
#-keep class androidx.leanback.app.BrowseSupportFragment { *; }
#-keep class io.reactivex.internal.operators.observable.ObservableDelaySubscriptionOther { *; }
#-keep class com.google.gson.internal.bind.TypeAdapters$7 { *; }
#-keep class com.google.gson.internal.bind.TypeAdapters { *; }
#-keep class androidx.leanback.widget.ItemAlignmentFacet$ItemAlignmentDef { *; }
#-keep class SmartTubeApp.app.models.playback.ui.OptionCategory { *; }
#-keep class SmartTubeApp.channels.UpdateChannelsReceiver { *; }
#-keep class androidx.core.view.ViewCompat { *; }
#-keep class androidx.work.impl.WorkManagerInitializer { *; }
#-keep class **$r8$backportedMethods$** { *; }
#-keep class kotlin.text.StringsKt__StringsJVMKt { *; }
#-keep class kotlin.ranges.RangesKt___RangesKt { *; }
#-keep class kotlin.jvm.functions.Function1 { *; }
#-keep class io.reactivex.schedulers.Schedulers { *; }

# Not sure why I've commented these out (probably the main dex was full)
#-keep class com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor { *; }
#-keep class kotlin.collections.builders.* { *; }
#-keep class androidx.room.** { *; }

# NOTE: Stable/Beta: VirusTotal (Google: Detected, Ikarus: Trojan.AndroidOS.Agent)
#-keep class SmartTubeApp.ui.main.MainApplication { *; }
#-keep class SmartTubeApp.prefs.MainUIData { *; }
#-keep class SmartTubeApp.app.presenters.ChannelUploadsPresenter { *; }

# NOTE: Fdroid: VirusTotal (Google: Detected, Ikarus: Trojan.AndroidOS.Agent)
#-keep class SmartTubeApp.app.presenters.dialogs.menu.providers.ContextMenuProvider { *; }
#-keep class com.google.android.exoplayer2.util.Util { *; }
#-keep class com.bumptech.glide.request.RequestOptions { *; }
#-keep class SmartTubeApp.ui.mod.leanback.playerglue.tooltips.TooltipCompatHandler { *; }