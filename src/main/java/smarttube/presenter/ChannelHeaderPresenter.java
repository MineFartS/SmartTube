package minefarts.smarttube.presenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

import minefarts.smarttube.leanback.widget.Row;
import minefarts.smarttube.leanback.widget.RowPresenter;
import minefarts.smarttube.leanback.widget.SearchBar;
import minefarts.smarttube.leanback.widget.SearchEditText;
import minefarts.smarttube.leanback.widget.SearchOrbView;
import minefarts.smarttube.leanback.widget.SpeechOrbView;
import minefarts.smarttube.leanback.widget.SpeechRecognitionCallback;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.KeyHelpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.helpers.PermissionHelpers;
import minefarts.smarttube.utils.MotherActivity;
import minefarts.smarttube.prefs.SearchData;
import minefarts.smarttube.BuildConfig;
import minefarts.smarttube.R;
import minefarts.smarttube.utils.ViewUtil;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;

import java.util.ArrayList;
import java.util.List;

public class ChannelHeaderPresenter extends RowPresenter {
    private static final String TAG = ChannelHeaderPresenter.class.getSimpleName();
    private static final String EXTRA_LEANBACK_BADGE_PRESENT = "LEANBACK_BADGE_PRESENT";
    private static final int REQUEST_SPEECH = 0x00000010;

    private Drawable mBadgeDrawable;
    private int mStatus;
    private String mTitle;

    public static class ChannelHeaderCallback extends Row {

        public boolean onSearchChange(String newQuery) {
            return false;
        }

        public boolean onSearchSubmit(String query) {
            return false;
        }

        public void onSearchSettingsClicked() {
            // NOP
        }

        public String getChannelTitle() {
            return null;
        }

    }

    @Override
    protected ViewHolder createRowViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View channelHeader = inflater.inflate(R.layout.channel_header, parent, false);
        init(channelHeader);

        setSelectEffectEnabled(ViewUtil.ROW_SELECT_EFFECT_ENABLED);

        return new ViewHolder(channelHeader);
    }

    private void init(View header) {

        Context context = header.getContext();
        SearchData searchData = SearchData.instance(context);
        
        SearchBar searchBar = header.findViewById(R.id.lb_search_bar);
        SearchOrbView searchOrbView = searchBar.findViewById(R.id.lb_search_bar_search_orb);
        SpeechOrbView speechOrbView = searchBar.findViewById(R.id.lb_search_bar_speech_orb);
        SearchEditText searchTextEditor = searchBar.findViewById(R.id.lb_search_text_editor);

        // Channel view settings icon
        SearchOrbView searchSettingsOrbView = searchBar.findViewById(R.id.search_settings_orb); 
        
        // Default recognizer. Used when there's no speech callbacks specified.
        searchBar.setSpeechRecognizer(SpeechRecognizer.createSpeechRecognizer(context));

        searchBar.setPermissionListener(() -> PermissionHelpers.verifyMicPermissions(context));

        // Select all on focus (easy clear previous search)
        searchTextEditor.setSelectAllOnFocus(true);

        searchOrbView.setOnFocusChangeListener((v, focused) -> {
            if (focused) {
                Helpers.hideKeyboard(context, v);
            }
        });

        searchSettingsOrbView.setOnFocusChangeListener((v, focused) -> {
            if (focused) {
                Helpers.hideKeyboard(context, v);
            }
        });

        OnFocusChangeListener previousListener = speechOrbView.getOnFocusChangeListener();
        speechOrbView.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                stopSpeechService(context);
            }

            // Fix: Enable edit field dynamic style: white/grey, listening/non listening
            if (previousListener != null) {
                previousListener.onFocusChange(v, focused);
            }
        });
    }

    @Override
    protected void onBindRowViewHolder(ViewHolder vh, Object item) {
        super.onBindRowViewHolder(vh, item);

        ChannelHeaderCallback provider = (ChannelHeaderCallback) item;
        SearchBar searchBar = vh.view.findViewById(R.id.lb_search_bar);
        Context context = searchBar.getContext();
        SearchOrbView searchOrbView = searchBar.findViewById(R.id.lb_search_bar_search_orb);
        SpeechOrbView speechOrbView = searchBar.findViewById(R.id.lb_search_bar_speech_orb);
        SearchEditText searchTextEditor = searchBar.findViewById(R.id.lb_search_text_editor);
        SearchOrbView searchSettingsOrbView = searchBar.findViewById(R.id.search_settings_orb);
        String channelName = provider.getChannelTitle();
        searchBar.setTitle(channelName != null ? channelName : context.getString(R.string.content_type_channel));
        
        searchBar.setSearchBarListener(new SearchBar.SearchBarListener() {
            @Override
            public void onSearchQueryChange(String query) {
                if (BuildConfig.DEBUG) Log.v(TAG, String.format("onSearchQueryChange %s %s", query,
                        null == provider ? "(null)" : provider));
                if (null != provider) {
                    retrieveResults(provider, query);
                }
            }

            @Override
            public void onSearchQuerySubmit(String query) {
                if (BuildConfig.DEBUG) Log.v(TAG, String.format("onSearchQuerySubmit %s", query));
                submitQuery(provider, query);
            }

            @Override
            public void onKeyboardDismiss(String query) {
                if (BuildConfig.DEBUG) Log.v(TAG, String.format("onKeyboardDismiss %s", query));
                // MOD: don't focus on results row after hiding keyboard
                //queryComplete();
            }
        });
        searchTextEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // NOP
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // NOP
            }

            @Override
            public void afterTextChanged(Editable s) {}
            
        });
        searchOrbView.setOnOrbClickedListener(v -> submitQuery(provider, getSearchBarText(searchTextEditor)));
        searchSettingsOrbView.setOnOrbClickedListener(v -> provider.onSearchSettingsClicked());

        if (null != mBadgeDrawable) {
            setBadgeDrawable(searchBar, mBadgeDrawable);
        }

        if (null != mTitle) {
            setTitle(searchBar, mTitle);
        }
    }

    private final class RecognizerIntentCallback implements SpeechRecognitionCallback {
        private final Context mContext;
        private final ChannelHeaderCallback mProvider;
        private final SearchBar mSearchBar;

        public RecognizerIntentCallback(Context context, ChannelHeaderCallback provider, SearchBar searchBar) {
            mContext = context;
            mProvider = provider;
            mSearchBar = searchBar;
        }

        @Override
        public void recognizeSpeech() {
            if (PermissionHelpers.hasMicPermissions(mContext)) {
                MessageHelpers.showMessage(mContext, R.string.disable_mic_permission);
            }

            try {
                if (mContext instanceof MotherActivity) {
                    ((MotherActivity) mContext).addOnResult(this::onActivityResult);
                    ((MotherActivity) mContext).startActivityForResult(getRecognizerIntent(mSearchBar), REQUEST_SPEECH);
                }
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Cannot find activity for speech recognizer", e);
            } catch (NullPointerException e) {
                Log.e(TAG, "Speech recognizer can't obtain applicationInfo", e);
            }
        }

        private void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_SPEECH) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String result = getRecognizerResult(data);
                        if (result != null) {
                            submitQuery(mProvider, result);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "Recognizer canceled");
                        break;
                }
            }
        }
    }

    /**
     * Returns an intent that can be used to request speech recognition.
     * Built from the base {@link RecognizerIntent#ACTION_RECOGNIZE_SPEECH} plus
     * extras:
     *
     * <ul>
     * <li>{@link RecognizerIntent#EXTRA_LANGUAGE_MODEL} set to
     * {@link RecognizerIntent#LANGUAGE_MODEL_FREE_FORM}</li>
     * <li>{@link RecognizerIntent#EXTRA_PARTIAL_RESULTS} set to true</li>
     * <li>{@link RecognizerIntent#EXTRA_PROMPT} set to the search bar hint text</li>
     * </ul>
     */
    private Intent getRecognizerIntent(SearchBar searchBar) {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        if (searchBar != null && searchBar.getHint() != null) {
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, searchBar.getHint());
        }
        recognizerIntent.putExtra(EXTRA_LEANBACK_BADGE_PRESENT, mBadgeDrawable != null);
        return recognizerIntent;
    }

    private String getRecognizerResult(Intent intent) {
        ArrayList<String> matches = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        return matches != null && matches.size() > 0 ? matches.get(0) : null;
    }

    private void submitQuery(ChannelHeaderCallback provider, String query) {
        if (query == null) {
            return;
        }
        
        if (null != provider) {
            provider.onSearchSubmit(query);
        }
    }

    private String getSearchBarText(SearchEditText searchTextEditor) {
        return searchTextEditor.getText().toString();
    }

    private void retrieveResults(ChannelHeaderCallback provider, String searchQuery) {
        if (BuildConfig.DEBUG) Log.v(TAG, "retrieveResults " + searchQuery);
        if (provider.onSearchChange(searchQuery)) {
            mStatus &= ~0x2;
        }
    }

    private void stopSpeechService(Context context) {}

    private void setTitle(SearchBar searchBar, String title) {
        mTitle = title;
        if (null != searchBar) {
            searchBar.setTitle(title);
        }
    }

    private void setBadgeDrawable(SearchBar searchBar, Drawable drawable) {
        mBadgeDrawable = drawable;
        if (null != searchBar) {
            searchBar.setBadgeDrawable(drawable);
        }
    }

    private void applyExternalQuery(ChannelHeaderCallback provider, SearchBar mSearchBar, String query, boolean submit) {
        if (query == null || mSearchBar == null) {
            return;
        }
        mSearchBar.setSearchQuery(query);
        if (submit) {
            submitQuery(provider, query);
        }
    }

    private void showListening(SpeechOrbView speechOrbView) {
        if (speechOrbView != null) {
            speechOrbView.showListening();
        }
    }

    private void showNotListening(SpeechOrbView speechOrbView) {
        if (speechOrbView != null) {
            speechOrbView.showNotListening();
        }
    }
}
