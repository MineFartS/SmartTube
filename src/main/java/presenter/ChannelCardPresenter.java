package minefarts.smarttube.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.R;
import minefarts.smarttube.presenter.base.LongClickPresenter;
import minefarts.smarttube.ui.browse.video.GridFragmentHelper;
import minefarts.smarttube.util.ViewUtil;
import minefarts.smarttube.misc.CardColors;

public class ChannelCardPresenter extends LongClickPresenter {
    
    private static final String TAG = VideoCardPresenter.class.getSimpleName();
    
    private CardColors mCardColors;
    private int mWidth;
    private int mHeight;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        Context context = parent.getContext();

        mCardColors = new CardColors(context);

        updateDimensions(context);

        View container = LayoutInflater.from(context).inflate(R.layout.channel_card, null);
        container.setBackgroundColor(mCardColors.DefaultBackgroundColor);

        TextView textView = container.findViewById(R.id.channel_title);
        textView.setBackgroundColor(mCardColors.DefaultBackgroundColor);
        textView.setTextColor(mCardColors.DefaultTextColor);

        container.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundColor = hasFocus ? mCardColors.SelectedBackgroundColor :
                    textView.getTag(R.id.channel_new_content) != null ? mCardColors.NewContentBackgroundColor : mCardColors.DefaultBackgroundColor;
            int textColor = hasFocus ? mCardColors.SelectedTextColor : mCardColors.DefaultTextColor;
            
            textView.setBackgroundColor(backgroundColor);
            textView.setTextColor(textColor);

            if (hasFocus) {
                ViewUtil.enableMarquee(textView);
            } else {
                ViewUtil.disableMarquee(textView);
            }
        });

        return new ViewHolder(container);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        Context context = viewHolder.view.getContext();
        Video video = (Video) item;

        ViewUtil.setDimensions(viewHolder.view.findViewById(R.id.channel_card_wrapper), mWidth, -1); // don't do auto height

        TextView textView = viewHolder.view.findViewById(R.id.channel_title);
        textView.setText(video.getTitle());

        // We should setup props each time because object may be reused by the underlying RecyclerView
        textView.setBackgroundColor(video.hasNewContent ? mCardColors.NewContentBackgroundColor : mCardColors.DefaultBackgroundColor);
        textView.setTag(R.id.channel_new_content, video.hasNewContent ? true : null);


        ImageView imageView = viewHolder.view.findViewById(R.id.channel_image);
        imageView.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(video.cardImageUrl)
                .apply(ViewUtil.glideOptions())
                .listener(mErrorListener)

                .into(imageView);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Remove references to images so that the garbage collector can free up memory.
        ImageView imageView = viewHolder.view.findViewById(R.id.channel_image);
        imageView.setImageDrawable(null);
    }

    private void updateDimensions(Context context) {
        Pair<Integer, Integer> dimens = getCardDimensPx(context);

        mWidth = dimens.first;
        mHeight = dimens.second;
    }

    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        return GridFragmentHelper.getCardDimensPx(
                context,
                R.dimen.channel_card_width,
                R.dimen.channel_card_height,
                1.0f, // Scale
                true
            );
    }

    private final RequestListener<Drawable> mErrorListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Glide load failed: " + e);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };
}
