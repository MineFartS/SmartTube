package SmartTubeApp.presenter.vineyard;

import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import com.liskovsoft.sharedutils.helpers.Helpers;
import SmartTubeApp.R;
import SmartTubeApp.app.models.search.vineyard.Tag;
import SmartTubeApp.app.models.search.vineyard.User;
import SmartTubeApp.presenter.base.LongClickPresenter;
import SmartTubeApp.ui.widgets.vineyard.TagCardView;

public class TagPresenter extends LongClickPresenter {
    private static int sDefaultBackgroundColor;
    private static int sDefaultTextColor;
    private static int sSelectedBackgroundColor;
    private static int sSelectedTextColor;

    private OnItemClickListener mClickListener;
    private OnItemSelectedListener mSelectedListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mSelectedListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClicked(Object item);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Object item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), Helpers.getThemeAttr(parent.getContext(), R.attr.cardDefaultBackground));
        sDefaultTextColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_default_text);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_selected_background_white);
        sSelectedTextColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_selected_text_grey);

        TagCardView cardView = new TagCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                updateCardTextColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        updateCardTextColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(TagCardView view, boolean selected) {
        view.setBackgroundColor(selected ? sSelectedBackgroundColor : sDefaultBackgroundColor);
    }

    private static void updateCardTextColor(TagCardView view, boolean selected) {
        view.setTextColor(selected ? sSelectedTextColor : sDefaultTextColor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        viewHolder.view.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClicked(item);
            }
        });
        viewHolder.view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && mSelectedListener != null) {
                mSelectedListener.onItemSelected(item);
            }
        });

        if (item instanceof Tag) {
            Tag post = (Tag) item;
            TagCardView cardView = (TagCardView) viewHolder.view;

            if (post.tag != null) {
                cardView.setCardText(post.tag);
                //cardView.setCardIcon(R.drawable.ic_tag);
            }
        } else if (item instanceof User) {
            User post = (User) item;
            TagCardView cardView = (TagCardView) viewHolder.view;

            if (post.username != null) {
                cardView.setCardText(post.username);
                cardView.setCardIcon(R.drawable.ic_user);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        viewHolder.view.setOnClickListener(null);
        viewHolder.view.setOnFocusChangeListener(null);
    }

}