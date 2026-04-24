

package SmartTubeApp.presenter;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import SmartTubeApp.app.models.data.Video;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        if (video != null) {
            viewHolder.getTitle().setText(video.getTitle());
            viewHolder.getSubtitle().setText(video.getAuthor());
            viewHolder.getBody().setText(video.getSecondTitle());
        }
    }
}
