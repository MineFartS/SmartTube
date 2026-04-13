

package androidx.fragment.app;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

@SuppressLint("BanParcelableUsage")
final class FragmentManagerState implements Parcelable {
    ArrayList<FragmentState> mActive;
    ArrayList<String> mAdded;
    BackStackState[] mBackStack;
    String mPrimaryNavActiveWho = null;
    int mNextFragmentIndex;

    public FragmentManagerState() {
    }

    public FragmentManagerState(Parcel in) {
        mActive = in.createTypedArrayList(FragmentState.CREATOR);
        mAdded = in.createStringArrayList();
        mBackStack = in.createTypedArray(BackStackState.CREATOR);
        mPrimaryNavActiveWho = in.readString();
        mNextFragmentIndex = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mActive);
        dest.writeStringList(mAdded);
        dest.writeTypedArray(mBackStack, flags);
        dest.writeString(mPrimaryNavActiveWho);
        dest.writeInt(mNextFragmentIndex);
    }

    public static final Parcelable.Creator<FragmentManagerState> CREATOR
            = new Parcelable.Creator<FragmentManagerState>() {
        @Override
        public FragmentManagerState createFromParcel(Parcel in) {
            return new FragmentManagerState(in);
        }

        @Override
        public FragmentManagerState[] newArray(int size) {
            return new FragmentManagerState[size];
        }
    };
}
