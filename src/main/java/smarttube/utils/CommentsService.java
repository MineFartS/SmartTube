package minefarts.smarttube.utils;

import minefarts.smarttube.utils.data.CommentGroup;
import io.reactivex.Observable;

public interface CommentsService {
    Observable<CommentGroup> getCommentsObserve(String key);
    Observable<Void> toggleLikeObserve(String key);
    Observable<Void> toggleDislikeObserve(String key);
}
