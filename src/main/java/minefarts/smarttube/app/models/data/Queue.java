package minefarts.smarttube.app.models.data;

import java.util.List;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.Playlist;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;

public class Queue {

    public static void add(Video video) {
        Playlist.instance().add(video);
    }

    public static void next(Video video) {
        Playlist.instance().next(video);
    }

    public static void remove(Video video) {
        Playlist.instance().remove(video);
    }

    public static boolean contains(Video video) {
        return Playlist.instance().contains(video);
    }

    public static Video getNext() {
        return Playlist.instance().getNext();
    }

    public static Video getPrevious() {
        return Playlist.instance().getPrevious();
    }

    public static void setCurrent(Video video) {
        Playlist.instance().setCurrent(video);
    }

    public static List<Video> getChangedItems() {
        return Playlist.instance().getChangedItems();
    }

    public static List<Video> getAllAfterCurrent() {
        return Playlist.instance().getAllAfterCurrent();
    }

    public static void onNewSession() {
        Playlist.instance().onNewSession();
    }

    public static void sync(Video origin) {
        Playlist.instance().sync(origin);
    }

    public static List<Video> getAll() {
        return Playlist.instance().getAll();
    }

}