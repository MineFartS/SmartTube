package com.liskovsoft.sharedutils;

import com.liskovsoft.sharedutils.data.CommentGroup;
import io.reactivex.Observable;

public interface CommentsService {
    Observable<CommentGroup> getCommentsObserve(String key);
    Observable<Void> toggleLikeObserve(String key);
    Observable<Void> toggleDislikeObserve(String key);
}
