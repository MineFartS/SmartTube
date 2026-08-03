package minefarts.smarttube.google.common.models.auth;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class ErrorResponse {
    @JsonPath("$.error")
    private String mError;

    @JsonPath("$.error_description")
    private String mErrorDescription;

    public String getError() {
        return mError;
    }

    public String getErrorDescription() {
        return mErrorDescription;
    }
}
