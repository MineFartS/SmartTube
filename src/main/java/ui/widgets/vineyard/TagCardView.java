package minefarts.smarttube.ui.widgets.vineyard;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;

import com.liskovsoft.sharedutils.helpers.Helpers;
import minefarts.smarttube.R;
import minefarts.smarttube.misc.CardColors;

public class TagCardView extends BaseCardView {

    private CardColors mCardColors;

    TextView mTagNameText;
    ImageView mResultImage;

    public TagCardView(Context context, int styleResId) {
        
        super(
            new ContextThemeWrapper(context, styleResId), 
            null, 
            0
        ); 
        
        buildLoadingCardView(context, styleResId);
    
    }

    public TagCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        
        super(
            getStyledContext(context, attrs, defStyleAttr), 
            attrs, 
            defStyleAttr
        );
        
        buildLoadingCardView(
            context,
            getImageCardViewStyle(context, attrs, defStyleAttr)
        );
    
    }

    @Override
    public void setSelected(boolean selected) {
        
        setBackgroundColor(selected ? mCardColors.SelectedBackgroundColor : mCardColors.DefaultBackgroundColor);
        setTextColor(selected ? mCardColors.SelectedTextColor : mCardColors.DefaultTextColor);
        
        super.setSelected(selected);

    }

    @Override
    public boolean dispatchHoverEvent(android.view.MotionEvent event) {
        
        int action = event.getAction();
        
        if (action == android.view.MotionEvent.ACTION_HOVER_ENTER 
            || action == android.view.MotionEvent.ACTION_HOVER_MOVE
        ) {
            setSelected(true);
            return true;
        } else if (action == android.view.MotionEvent.ACTION_HOVER_EXIT) {
            setSelected(false);
            return true;
        }

        return super.dispatchHoverEvent(event);
    }

    private void buildLoadingCardView(Context context, int styleResId) {

        mCardColors = new CardColors(context);

        setFocusable(false);
        setFocusableInTouchMode(false);
        setCardType(CARD_TYPE_MAIN_ONLY);
        setBackgroundResource(R.color.primary_light);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_tag_card, this);

        mTagNameText = view.findViewById(R.id.text_tag_name);
        mResultImage = view.findViewById(R.id.image_icon);
        
        TypedArray cardAttrs =
                getContext().obtainStyledAttributes(
                        styleResId, R.styleable.lbImageCardView);
        cardAttrs.recycle();
    }

    public void setCardText(String string) {
        mTagNameText.setText(string);
    }

    public void setCardIcon(int resource) {
        mResultImage.setVisibility(View.VISIBLE);
        mResultImage.setImageDrawable(ContextCompat.getDrawable(getContext(), resource));
    }

    private static Context getStyledContext(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = getImageCardViewStyle(context, attrs, defStyleAttr);
        return new ContextThemeWrapper(context, style);
    }

    private static int getImageCardViewStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        // Read style attribute defined in XML layout.
        int style = null == attrs ? 0 : attrs.getStyleAttribute();
        if (0 == style) {
            // Not found? Read global ImageCardView style from Theme attribute.
            TypedArray styledAttrs =
                    context.obtainStyledAttributes(
                            R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(
                    R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public TagCardView(Context context) {
        this(context, null);
    }

    public TagCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageCardViewStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setTextColor(int color) {
        if (mTagNameText != null) {
            mTagNameText.setTextColor(color);
        }
    }
}
