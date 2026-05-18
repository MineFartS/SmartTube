package com.liskovsoft.sharedutils.actions;

import com.liskovsoft.sharedutils.common.helpers.PostDataHelper;

public class ActionsApiHelper {
    
    private static final String VIDEO_ID_TEMPLATE = "\"target\":{\"videoId\":\"%s\"}";
    
    public static String getLikeActionQuery(String videoId) {
        String likeTemplate = String.format(VIDEO_ID_TEMPLATE, videoId);
        return PostDataHelper.createQueryTV(likeTemplate);
    }

    public static String getEmptyQuery() {
        return PostDataHelper.createQueryTV("\"nop\":\"false\"");
    }
}
