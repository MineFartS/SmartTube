package minefarts.smarttube.misc;

import android.content.Context;
import androidx.core.content.ContextCompat;

import minefarts.smarttube.R;
import minefarts.sharedutils.helpers.Helpers;

public class CardColors {

    public final int DefaultBackgroundColor;
    public final int SelectedBackgroundColor;

    public final int DefaultTextColor;
    public final int SelectedTextColor;

    public final int NewContentBackgroundColor;
    
    public CardColors(Context context) {

        DefaultBackgroundColor = ContextCompat.getColor(context, Helpers.getThemeAttr(context, R.attr.cardDefaultBackground));

        SelectedBackgroundColor = ContextCompat.getColor(context, R.color.card_selected_background_white);


        DefaultTextColor = ContextCompat.getColor(context, R.color.card_default_text);
        
        SelectedTextColor = ContextCompat.getColor(context, R.color.card_selected_text_grey);


        NewContentBackgroundColor = ContextCompat.getColor(context, R.color.dark_red);

    }

}