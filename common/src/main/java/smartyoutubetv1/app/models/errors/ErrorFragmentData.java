package smartyoutubetv1.app.models.errors;

public interface ErrorFragmentData {
    void onAction();
    String getMessage();
    String getActionText();
}
