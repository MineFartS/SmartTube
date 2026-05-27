package minefarts.smarttube.utils.oauth;

import minefarts.smarttube.google.common.models.auth.UserCode;

public class SignInCode {

    private final UserCode mUserCode;

    public SignInCode(UserCode userCode) {
        mUserCode = userCode;
    }
    
    public String getSignInCode() {
        return mUserCode.getUserCode();
    }

    public String getSignInUrl() {
        return mUserCode.getVerificationUrl();
    }

}
