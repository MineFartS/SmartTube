package SmartTubeApp.app.models.search;

import android.text.TextUtils;

import com.liskovsoft.youtubeapi.ContentService;
import com.liskovsoft.youtubeapi.ServiceManager;
import com.liskovsoft.sharedutils.mylogger.Log;
import SmartTubeApp.app.models.search.vineyard.Tag;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;
import io.reactivex.disposables.Disposable;

public class MediaServiceSearchTagProvider implements SearchTagsProvider {
    
    private static final String TAG = MediaServiceSearchTagProvider.class.getSimpleName();
    
    private final ContentService mContentService;
    private Disposable mTagsAction;

    public MediaServiceSearchTagProvider() {

        ServiceManager service = YouTubeServiceManager.instance();

        mContentService = service.getContentService();

    }

    @Override
    public void search(String query, ResultsCallback callback) {
        
        RxHelper.disposeActions(mTagsAction);

        mTagsAction = mContentService.getSearchTagsObserve(query).subscribe(
                tags -> callback.onResults(Tag.from(tags)),
                error -> Log.e(TAG, "Result is empty. Just ignore it. Error msg: %s", error.getMessage())
        );
    
    }

}
