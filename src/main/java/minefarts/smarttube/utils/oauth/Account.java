package minefarts.smarttube.utils.oauth;

public interface Account {
    int getId();
    String getName();
    String getEmail();
    String getAvatarImageUrl();
    boolean isSelected();
    boolean isEmpty();
}
