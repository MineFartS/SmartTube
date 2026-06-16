package minefarts.smarttube.app.models.data;

import androidx.annotation.Nullable;

import minefarts.smarttube.google.common.helpers.ServiceHelper;
import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class DislikesResult {

    @JsonPath("$.id")
    private String id;

    @JsonPath("$.dateCreated")
    private String dateCreated;
    
    @JsonPath("$.likes")
    private Integer likes;
    
    @JsonPath("$.dislikes")
    private Integer dislikes;
    
    @JsonPath("$.rating")
    private Integer rating;
    
    @JsonPath("$.viewCount")
    private Long viewCount;
    
    @JsonPath("$.deleted")
    private Boolean deleted;
        
    @Nullable
    public String getVideoId() {
        return id;
    }

    public long getViewCount() {
        return viewCount != null ? viewCount : 0L;
    }

    @Nullable
    public String getDislikeCount() {
        if (dislikes != null && dislikes > 0) {
            return ServiceHelper.prettyCount(dislikes);
        } else {
            return null;
        }
    }

    @Nullable
    public String getLikeCount() {
        if (likes != null && likes > 0) {
            return ServiceHelper.prettyCount(likes);
        } else {
            return null;
        }
    }
    
}