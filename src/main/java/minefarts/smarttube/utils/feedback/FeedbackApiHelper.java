package minefarts.smarttube.utils.feedback;

import minefarts.smarttube.utils.common.helpers.PostDataHelper;

public class FeedbackApiHelper {
    private static final String NOT_INTERESTED_QUERY = "\"feedbackTokens\":[\"%s\"]";

    public static String getNotInterestedQuery(String feedbackToken) {
        String channelTemplate = String.format(NOT_INTERESTED_QUERY, feedbackToken);
        return PostDataHelper.createQueryTV(channelTemplate);
    }
}
