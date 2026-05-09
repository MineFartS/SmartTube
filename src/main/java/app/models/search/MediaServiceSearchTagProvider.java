package minefarts.smarttube.app.models.search;

import android.text.TextUtils;

import com.liskovsoft.sharedutils.service.ContentService;
import minefarts.smarttube.misc.ServiceManager;
import com.liskovsoft.sharedutils.mylogger.Log;
import minefarts.smarttube.app.models.search.vineyard.Tag;
import com.liskovsoft.sharedutils.rx.RxHelper;
import io.reactivex.disposables.Disposable;

public class MediaServiceSearchTagProvider implements SearchTagsProvider {
    
    private static final String TAG = MediaServiceSearchTagProvider.class.getSimpleName();
    
    private final ContentService mContentService;
    private Disposable mTagsAction;

    public MediaServiceSearchTagProvider() {
        mContentService = ServiceManager.getContentService();
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
