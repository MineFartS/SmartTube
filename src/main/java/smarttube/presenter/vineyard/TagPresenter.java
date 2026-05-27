package minefarts.smarttube.presenter.vineyard;

import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.search.vineyard.Tag;
import minefarts.smarttube.app.models.search.vineyard.User;
import minefarts.smarttube.presenter.base.LongClickPresenter;
import minefarts.smarttube.ui.widgets.vineyard.TagCardView;

public class TagPresenter extends LongClickPresenter {

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
        
        TagCardView cardView = new TagCardView(parent.getContext());

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setSelected(false);
        
        return new ViewHolder(cardView);
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