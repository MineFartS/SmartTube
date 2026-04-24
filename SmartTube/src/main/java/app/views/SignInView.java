package SmartTubeApp.app.views;

public interface SignInView {
    void showCode(String userCode, String signInUrl);
    void close();
}
