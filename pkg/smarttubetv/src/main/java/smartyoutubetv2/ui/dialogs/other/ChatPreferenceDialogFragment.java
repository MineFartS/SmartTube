package smartyoutubetv2.ui.dialogs.other;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import com.bumptech.glide.Glide;
import smartyoutubetv1.app.models.playback.ui.ChatReceiver;
import smartyoutubetv2.R;
import smartyoutubetv2.ui.mod.leanback.preference.LeanbackPreferenceDialogFragment;
import smartyoutubetv2.ui.widgets.chat.ChatItemMessage;
import smartyoutubetv2.util.ViewUtil;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class ChatPreferenceDialogFragment extends LeanbackPreferenceDialogFragment {
    private static final String SENDER_ID = ChatPreferenceDialogFragment.class.getSimpleName();
    private boolean mIsTransparent;
    private ChatReceiver mChatReceiver;
    private CharSequence mDialogTitle;

    public static ChatPreferenceDialogFragment newInstance(ChatReceiver chatReceiver, String key) {
        final Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);

        final ChatPreferenceDialogFragment
                fragment = new ChatPreferenceDialogFragment();
        fragment.setArguments(args);
        fragment.mChatReceiver = chatReceiver;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            final DialogPreference preference = getPreference();
            mDialogTitle = preference.getDialogTitle();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat_preference_fragment, container,
                false);

        final CharSequence title = mDialogTitle;
        if (!TextUtils.isEmpty(title)) {
            final TextView titleView = (TextView) view.findViewById(R.id.decor_title);
            titleView.setText(title);
        }

        MessagesList messagesList = (MessagesList) view.findViewById(R.id.messagesList);
        MessagesListAdapter<ChatItemMessage> adapter = new MessagesListAdapter<>(SENDER_ID, (imageView, url, payload) ->
                Glide.with(view.getContext())
                    .load(url)
                    .apply(ViewUtil.glideOptions())
                    .circleCrop() // resize image
                    .into(imageView));
        messagesList.setAdapter(adapter);

        if (mChatReceiver != null) {
            mChatReceiver.setCallback(chatItem -> adapter.addToStart(ChatItemMessage.from(chatItem), true));
        }

        if (mIsTransparent) {
            ViewUtil.enableTransparentDialog(getActivity(), view);
        }

        return view;
    }

    public void enableTransparent(boolean enable) {
        mIsTransparent = enable;
    }
}
