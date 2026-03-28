package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.menu.providers.ContextMenuManager;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.prefs.common.DataChangeBase;
import com.liskovsoft.smartyoutubetv2.common.utils.DataStore;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainUIData extends DataChangeBase implements ProfileChangeListener {
    
    public static final int CARD_PREVIEW_DISABLED = 0;
    
    public static final int CARD_PREVIEW_MUTED = 1;
    
    public static final int CARD_PREVIEW_FULL = 2;
    
    public static final int CHANNEL_SORTING_NEW_CONTENT = 0;
    
    public static final int CHANNEL_SORTING_NAME = 1;
    
    public static final int CHANNEL_SORTING_DEFAULT = 2;
    
    public static final int CHANNEL_SORTING_LAST_VIEWED = 3;
    
    public static final int CHANNEL_SORTING_NAME2 = 4;
    
    public static final int PLAYLISTS_STYLE_GRID = 0;
    
    public static final int PLAYLISTS_STYLE_ROWS = 1;
    
    public static final long MENU_ITEM_RECENT_PLAYLIST = 1;
    
    public static final long MENU_ITEM_ADD_TO_QUEUE = 1 << 1;
   
    public static final long MENU_ITEM_PIN_TO_SIDEBAR = 1 << 2;
   
    public static final long MENU_ITEM_SHARE_LINK = 1 << 3;
   
    public static final long MENU_ITEM_SELECT_ACCOUNT = 1 << 4;
   
    public static final long MENU_ITEM_NOT_INTERESTED = 1 << 5;
   
    public static final long MENU_ITEM_REMOVE_FROM_HISTORY = 1 << 6;
   
    public static final long MENU_ITEM_MOVE_SECTION_UP = 1 << 7;
   
    public static final long MENU_ITEM_MOVE_SECTION_DOWN = 1 << 8;
   
    public static final long MENU_ITEM_OPEN_DESCRIPTION = 1 << 9;
   
    public static final long MENU_ITEM_RENAME_SECTION = 1 << 10;
   
    public static final long MENU_ITEM_PLAY_VIDEO = 1 << 11;
   
    public static final long MENU_ITEM_SAVE_REMOVE_PLAYLIST = 1 << 12;
   
    public static final long MENU_ITEM_ADD_TO_PLAYLIST = 1 << 13;
   
    public static final long MENU_ITEM_SUBSCRIBE = 1 << 14;
   
    public static final long MENU_ITEM_CREATE_PLAYLIST = 1 << 15;
      
    public static final long MENU_ITEM_ADD_TO_NEW_PLAYLIST = 1 << 17;
   
    public static final long MENU_ITEM_SHARE_EMBED_LINK = 1 << 18;
   
    public static final long MENU_ITEM_SHOW_QUEUE = 1 << 19;
   
    public static final long MENU_ITEM_PLAYLIST_ORDER = 1 << 20;
   
    public static final long MENU_ITEM_TOGGLE_HISTORY = 1 << 21;
   
    public static final long MENU_ITEM_CLEAR_HISTORY = 1 << 22;
   
    public static final long MENU_ITEM_UPDATE_CHECK = 1 << 23;
   
    public static final long MENU_ITEM_OPEN_CHANNEL = 1 << 24;
   
    public static final long MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS = 1 << 25;
   
    public static final long MENU_ITEM_MARK_AS_WATCHED = 1 << 27;
   
    public static final long MENU_ITEM_EXCLUDE_FROM_CONTENT_BLOCK = 1 << 28;
   
    public static final long MENU_ITEM_OPEN_PLAYLIST = 1 << 29;
   
    public static final long MENU_ITEM_EXIT_FROM_PIP = 1 << 30;
   
    public static final long MENU_ITEM_OPEN_COMMENTS = 1L << 31;
   
    public static final long MENU_ITEM_SHARE_QR_LINK = 1L << 32;
   
    public static final long MENU_ITEM_PLAY_NEXT = 1L << 33;
   
    public static final long MENU_ITEM_RENAME_PLAYLIST = 1L << 34;
   
    public static final long MENU_ITEM_NOT_RECOMMEND_CHANNEL = 1L << 35;
   
    public static final int TOP_BUTTON_BROWSE_ACCOUNTS = 1;
   
    public static final int TOP_BUTTON_CHANGE_LANGUAGE = 1 << 1;
   
    public static final int TOP_BUTTON_SEARCH = 1 << 2;
   
    public static final int TOP_BUTTON_DEFAULT = TOP_BUTTON_SEARCH | TOP_BUTTON_BROWSE_ACCOUNTS;
   
    public static final long MENU_ITEM_DEFAULT = 
        MENU_ITEM_NOT_INTERESTED | 
        MENU_ITEM_MARK_AS_WATCHED |
        MENU_ITEM_NOT_RECOMMEND_CHANNEL |
        MENU_ITEM_REMOVE_FROM_HISTORY | 
        MENU_ITEM_SAVE_REMOVE_PLAYLIST | 
        MENU_ITEM_ADD_TO_PLAYLIST | 
        MENU_ITEM_CREATE_PLAYLIST | 
        MENU_ITEM_OPEN_CHANNEL | 
        MENU_ITEM_OPEN_PLAYLIST | 
        MENU_ITEM_SUBSCRIBE;
    
    private static final Long[] MENU_ITEM_DEFAULT_ORDER = {
        MENU_ITEM_MARK_AS_WATCHED,
        MENU_ITEM_NOT_INTERESTED, 
        MENU_ITEM_NOT_RECOMMEND_CHANNEL, 

        MENU_ITEM_EXIT_FROM_PIP, 
        MENU_ITEM_PLAY_VIDEO,  

        MENU_ITEM_REMOVE_FROM_HISTORY,
        MENU_ITEM_RECENT_PLAYLIST, 
        MENU_ITEM_ADD_TO_PLAYLIST, 
        MENU_ITEM_CREATE_PLAYLIST, 
        MENU_ITEM_RENAME_PLAYLIST,
        MENU_ITEM_ADD_TO_NEW_PLAYLIST, 

        MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS,
         
        MENU_ITEM_PLAYLIST_ORDER, 
        MENU_ITEM_PLAY_NEXT, 
        MENU_ITEM_ADD_TO_QUEUE, 
        MENU_ITEM_SHOW_QUEUE, 
        MENU_ITEM_OPEN_CHANNEL,
        MENU_ITEM_OPEN_PLAYLIST, 
        MENU_ITEM_SUBSCRIBE, 
        MENU_ITEM_EXCLUDE_FROM_CONTENT_BLOCK, 
        MENU_ITEM_PIN_TO_SIDEBAR, 
        MENU_ITEM_SAVE_REMOVE_PLAYLIST,
        MENU_ITEM_OPEN_DESCRIPTION, 
        MENU_ITEM_OPEN_COMMENTS, 
        MENU_ITEM_SHARE_LINK, 
        MENU_ITEM_SHARE_EMBED_LINK, 
        MENU_ITEM_SHARE_QR_LINK,
        MENU_ITEM_SELECT_ACCOUNT, 
        MENU_ITEM_TOGGLE_HISTORY, 
        MENU_ITEM_CLEAR_HISTORY, 
        MENU_ITEM_MOVE_SECTION_UP, 
        MENU_ITEM_MOVE_SECTION_DOWN,
        MENU_ITEM_UPDATE_CHECK
    };

    @SuppressLint("StaticFieldLeak")
    private static MainUIData sInstance;
    
    private final Context mContext;
    
    private final AppPrefs mPrefs;

    private final DataStore mDataStore;
                
    private int mCardTitleLinesNum;
    
    private float mUIScale;
    
    private int mChannelCategorySorting;
        
    private long mMenuItems;
    
    private ArrayList<Long> mMenuItemsOrdered;
    
    private int mCardPreviewType;
        
    private MainUIData(Context context) {

        mContext = context;

        mDataStore = new DataStore(context, "main_ui_data2");

        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);

        restoreState();

    }

    public static MainUIData instance(Context context) {

        if (sInstance == null) {
            sInstance = new MainUIData(context.getApplicationContext());
        }

        return sInstance;

    }

    public int getCardTitleLinesNum() {
        return mCardTitleLinesNum;
    }

    public void setCartTitleLinesNum(int lines) {
        
        mCardTitleLinesNum = lines;
        persistState();

    }

    public float getUIScale() {
        return mUIScale;
    }

    public void setUIScale(float scale) {
        
        mUIScale = scale;
        persistState();
    
    }

    public int getChannelCategorySorting() {
        return mChannelCategorySorting;
    }

    public void setChannelCategorySorting(int type) {
        
        mChannelCategorySorting = type;
        persistState();
    
    }

    public boolean isMenuItemEnabled(long menuItems) {
        return (mMenuItems & menuItems) == menuItems;
    }

    public void setMenuItemEnabled(long menuItems) {
        mMenuItems |= menuItems;
        persistState();
    }

    public void setMenuItemDisabled(long menuItems) {
        mMenuItems &= ~menuItems;
        persistState();
    }

    public ArrayList<Long> getMenuItemsOrdered() {
        return (ArrayList) Collections.unmodifiableList((List) mMenuItemsOrdered);
    }

    public int getMenuItemIndex(long menuItem) {
        return mMenuItemsOrdered.indexOf(menuItem);
    }

    public void setMenuItemIndex(int index, Long menuItem) {

        mMenuItemsOrdered.remove(menuItem);

        if (index <= mMenuItemsOrdered.size()) {
            mMenuItemsOrdered.add(index, menuItem);
        } else {
            mMenuItemsOrdered.add(menuItem);
        }

        persistState();

    }

    public int getCardPreviewType() {
        return mCardPreviewType;
    }

    public void setCardPreviewType(int type) {
        mCardPreviewType = type;
        persistState();
    }

    private void restoreState() {

        /* 0 */ mUIScale = mDataStore.get(0, 1.0f);
        /* 1 */ mChannelCategorySorting = mDataStore.get(1, CHANNEL_SORTING_LAST_VIEWED);
        /* 2 */ mCardTitleLinesNum = mDataStore.get(2, 1);
        /* 3 */ mMenuItems = mDataStore.get(3, MENU_ITEM_DEFAULT);
        /* 4 */ mMenuItemsOrdered = mDataStore.get(4, new ArrayList());
        /* 5 */ mCardPreviewType = mDataStore.get(5, CARD_PREVIEW_DISABLED);

        int idx = -1;
        for (Long menuItem : MENU_ITEM_DEFAULT_ORDER) {
            
            idx++;
            
            if (!mMenuItemsOrdered.contains(menuItem)) {
            
                if (idx < mMenuItemsOrdered.size()) {
                    mMenuItemsOrdered.add(idx, menuItem);
                } else {
                    mMenuItemsOrdered.add(menuItem);
                }

                boolean isEnabled = (MENU_ITEM_DEFAULT & menuItem) == menuItem;

                if (isEnabled) {
                    mMenuItems |= menuItem;
                }

            }

        }

        for (ContextMenuProvider provider : new ContextMenuManager(mContext).getProviders()) {
            
            if (!mMenuItemsOrdered.contains(provider.getId())) {
                mMenuItemsOrdered.add(provider.getId());
            }

        }
        
        updateDefaultValues();
    
    }

    public void persistState() {

        /* 0 */ mDataStore.put(0, mUIScale); 
        /* 1 */ mDataStore.put(1, mChannelCategorySorting); 
        /* 2 */ mDataStore.put(2, mCardTitleLinesNum);
        /* 3 */ mDataStore.put(3, mMenuItems);
        /* 4 */ mDataStore.put(4, mMenuItemsOrdered);
        /* 5 */ mDataStore.put(5, mCardPreviewType);

    }

    private void updateDefaultValues() {

        // Enable only certain items (not all, like it was)
        if (mMenuItems >>> 30 == 0b1) { // check leftmost bit (old format)
            int bits = 32 - 27;
            mMenuItems = mMenuItems << bits >>> bits; // remove auto enabled bits
        }

        if (mChannelCategorySorting == CHANNEL_SORTING_NAME2) {
            mChannelCategorySorting = CHANNEL_SORTING_NAME;
        }

        if (mChannelCategorySorting == CHANNEL_SORTING_DEFAULT) {
            mChannelCategorySorting = CHANNEL_SORTING_LAST_VIEWED;
        }

    }

    @Override
    public void onProfileChanged() {

        restoreState();
        onDataChange();

    }

}
