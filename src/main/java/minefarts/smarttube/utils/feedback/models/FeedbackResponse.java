package minefarts.smarttube.utils.feedback.models;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class FeedbackResponse {
    @JsonPath("$.feedbackResponses[0].isProcessed")
    private boolean mIsFeedbackProcessed;

    public boolean isFeedbackProcessed() {
        return mIsFeedbackProcessed;
    }
}
