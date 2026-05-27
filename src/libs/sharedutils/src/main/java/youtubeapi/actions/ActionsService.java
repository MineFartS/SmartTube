package minefarts.sharedutils.actions;

import androidx.annotation.NonNull;

import minefarts.sharedutils.mylogger.Log;
import minefarts.sharedutils.actions.models.ActionResult;
import minefarts.sharedutils.browse.v1.BrowseService;
import minefarts.googlecommon.common.helpers.RetrofitHelper;

import retrofit2.Call;

public class ActionsService {
    
    private final ActionsApi mActionsApi;

    public ActionsService() {
        mActionsApi = RetrofitHelper.create(ActionsApi.class);
    }

    public void setLike(String videoId) {
        Call<ActionResult> wrapper =
                mActionsApi.setLike(ActionsApiHelper.getLikeActionQuery(videoId));

        RetrofitHelper.get(wrapper); // ignore result
    }

    public void removeLike(String videoId) {
        Call<ActionResult> wrapper =
                mActionsApi.removeLike(ActionsApiHelper.getLikeActionQuery(videoId));

        RetrofitHelper.get(wrapper); // ignore result
    }

    public void setDislike(String videoId) {
        Call<ActionResult> wrapper =
                mActionsApi.setDislike(ActionsApiHelper.getLikeActionQuery(videoId));

        RetrofitHelper.get(wrapper); // ignore result
    }

    public void removeDislike(String videoId) {
        Call<ActionResult> wrapper =
                mActionsApi.removeDislike(ActionsApiHelper.getLikeActionQuery(videoId));

        RetrofitHelper.get(wrapper); // ignore result
    }

    public void clearSearchHistory() {
        // Empty start suggestions fix: use anonymous search
        //boolean skipAuth = getBrowseService().getSuggestToken() == null;

        Call<Void> wrapper = mActionsApi.clearSearchHistory(ActionsApiHelper.getEmptyQuery());
        RetrofitHelper.get(wrapper);
    }

}
