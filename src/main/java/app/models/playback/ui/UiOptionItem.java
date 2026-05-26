package minefarts.smarttube.app.models.playback.ui;

import minefarts.smarttube.exoplayer.selector.FormatItem;

import java.util.ArrayList;
import java.util.List;

public class UiOptionItem {
    
    private int mId;
    private CharSequence mTitle;
    private CharSequence mDescription;
    private boolean mIsSelected;
    private FormatItem mFormat;
    private OptionCallback mCallback;
    private Object mData;
    private UiOptionItem[] mRequiredItems;
    private UiOptionItem[] mRadioItems;
    private ChatReceiver mChatReceiver;
    private CommentsReceiver mCommentsReceiver;

    public static List<UiOptionItem> from(List<FormatItem> formats, OptionCallback callback) {
        return from(formats, callback, null);
    }

    public static List<UiOptionItem> from(List<FormatItem> formats, OptionCallback callback, String defaultTitle) {
        if (formats == null) {
            return null;
        }

        List<UiOptionItem> options = new ArrayList<>();

        for (FormatItem format : formats) {
            options.add(from(format, callback, defaultTitle));
        }

        return options;
    }

    public static UiOptionItem from(FormatItem format, OptionCallback callback) {
        return from(format, callback, null);
    }

    public static UiOptionItem from(FormatItem format, OptionCallback callback, String defaultTitle) {
        if (format == null) {
            return null;
        }

        UiOptionItem uiOptionItem = new UiOptionItem();

        uiOptionItem.mTitle = format.isDefault() ? defaultTitle : format.getTitle();
        uiOptionItem.mIsSelected = format.isSelected();
        uiOptionItem.mFormat = format;
        uiOptionItem.mCallback = callback;

        return uiOptionItem;
    }

    public static UiOptionItem from(CharSequence title) {
        return from(title, (OptionCallback) null);
    }

    public static UiOptionItem from(CharSequence title, OptionCallback callback) {
        return from(title, callback, false);
    }

    public static UiOptionItem from(CharSequence title, OptionCallback callback, boolean isChecked) {
        return from(title, callback, isChecked, null);
    }

    public static UiOptionItem from(CharSequence title, CharSequence description, OptionCallback callback, boolean isChecked) {
        return from(title, description, callback, isChecked, null);
    }

    public static UiOptionItem from(CharSequence title, OptionCallback callback, boolean isChecked, Object data) {
        return from(title, null, callback, isChecked, data);
    }

    public static UiOptionItem from(CharSequence title, CharSequence description, OptionCallback callback, boolean isChecked, Object data) {
        UiOptionItem uiOptionItem = new UiOptionItem();

        uiOptionItem.mTitle = title;
        uiOptionItem.mDescription = description;
        uiOptionItem.mIsSelected = isChecked;
        uiOptionItem.mCallback = callback;
        uiOptionItem.mData = data;

        return uiOptionItem;
    }

    public static UiOptionItem from(CharSequence title, ChatReceiver chatReceiver) {
        UiOptionItem uiOptionItem = new UiOptionItem();
        uiOptionItem.mTitle = title;
        uiOptionItem.mChatReceiver = chatReceiver;

        return uiOptionItem;
    }

    public static UiOptionItem from(CharSequence title, CommentsReceiver commentsReceiver) {
        UiOptionItem uiOptionItem = new UiOptionItem();
        uiOptionItem.mTitle = title;
        uiOptionItem.mCommentsReceiver = commentsReceiver;

        return uiOptionItem;
    }

    public static FormatItem toFormat(UiOptionItem option) {
        if (option instanceof UiOptionItem) {
            return ((UiOptionItem) option).mFormat;
        }

        return null;
    }

    public int getId() {
        return mId;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void onSelect(boolean isSelected) {
        mIsSelected = isSelected;

        if (mCallback != null) {
            mCallback.onSelect(this);
        }
    }

    public Object getData() {
        return mData;
    }

    public void setRequired(UiOptionItem... items) {
        if (items == null || items.length == 0) {
            mRequiredItems = null;
        }

        mRequiredItems = items;
    }

    public UiOptionItem[] getRequired() {
        return mRequiredItems;
    }

    public void setRadio(UiOptionItem... items) {
        if (items == null || items.length == 0) {
            mRadioItems = null;
        }

        mRadioItems = items;
    }

    public UiOptionItem[] getRadio() {
        return mRadioItems;
    }

    public ChatReceiver getChatReceiver() {
        return mChatReceiver;
    }

    public CommentsReceiver getCommentsReceiver() {
        return mCommentsReceiver;
    }
    
}
