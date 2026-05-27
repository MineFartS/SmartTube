package minefarts.sharedutils.feedback.models;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

public class FeedbackResponse {
    @JsonPath("$.feedbackResponses[0].isProcessed")
    private boolean mIsFeedbackProcessed;

    public boolean isFeedbackProcessed() {
        return mIsFeedbackProcessed;
    }
}
