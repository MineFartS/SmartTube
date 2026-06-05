package minefarts.smarttube.utils.actions;

import androidx.annotation.NonNull;

import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.actions.models.ActionResult;
import minefarts.smarttube.utils.browse.BrowseService;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;

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

}
